package org.migor.feedless.pipeline

import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.ClickXpathActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.actions.WaitActionEntity
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.data.jpa.repositories.SourceDAO
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.mail.MailForwardDAO
import org.migor.feedless.mail.MailForwardEntity
import org.migor.feedless.mail.MailService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.cron}")
@Transactional(propagation = Propagation.NEVER)
class PipelineJob internal constructor() {

  private val log = LoggerFactory.getLogger(PipelineJob::class.simpleName)

  @Autowired
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  @Autowired
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var sourceDAO: SourceDAO

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var pluginService: PluginService

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var mailService: MailService

  @Autowired
  private lateinit var repositoryHarvester: RepositoryHarvester

  @Autowired
  private lateinit var mailForwardDAO: MailForwardDAO

  @Scheduled(fixedDelay = 6245, initialDelay = 20000)
  @Transactional
  fun executePlugins() {
    val corrId = newCorrId()
    documentPipelineJobDAO.findAllPendingBatched()
      .groupBy { it.documentId }
      .map { processPlugins(newCorrId(3, corrId), it.key, it.value) }
  }

  @Scheduled(fixedDelay = 5245, initialDelay = 20000)
  @Transactional
  fun executeScrapes() {
    val corrId = newCorrId()
    sourcePipelineJobDAO.findAllPendingBatched()
      .groupBy { it.sourceId }
      .map { processSourcePipeline(newCorrId(3, corrId), it.key, it.value) }
  }

  private fun processSourcePipeline(corrId: String, sourceId: UUID, jobs: List<SourcePipelineJobEntity>) {
    log.info("[$corrId] ${jobs.size} processSourcePipeline for source $sourceId")
    val source = sourceDAO.findById(sourceId).orElseThrow()

    val job = jobs.first()
    job.status = PipelineJobStatus.IN_PROGRESS
    sourcePipelineJobDAO.save(job)

    try {
      if (job.attempt > 3) {
        throw IllegalArgumentException("max attempts reached")
      }

      try {
        repositoryHarvester.scrapeSource(corrId, patchRequestUrl(source, job.url)).block()
        job.status = PipelineJobStatus.SUCCEEDED
        job.updateStatus()
        log.info("[$corrId] job ${job.id} done")
      } catch (e: ResumableHarvestException) {
        log.info("[$corrId] delaying: ${e.message}")
        job.coolDownUntil = Date(System.currentTimeMillis() + e.nextRetryAfter.toMillis())
        job.attempt += 1
      }

    } catch (e: Exception) {
      log.warn("[$corrId] aborting scrape job, cause ${e.message}")
      job.status = PipelineJobStatus.FAILED
      job.updateStatus()
      job.logs = e.message
    }
    try {
      sourcePipelineJobDAO.save(job)
    } catch (e: Exception) {
      log.warn("[$corrId] ${e.message}]")
    }
  }

  private fun patchRequestUrl(source: SourceEntity, url: String): SourceEntity {
    val newSource = source.clone()
    newSource.actions = source.actions.mapNotNull { when(it) {
      is FetchActionEntity -> it.clone()
      is ExecuteActionEntity -> it.clone()
      is ClickPositionActionEntity -> it.clone()
      is ClickXpathActionEntity -> it.clone()
      is DomActionEntity -> it.clone()
      is ExtractBoundingBoxActionEntity -> it.clone()
      is ExtractXpathActionEntity -> it.clone()
      is HeaderActionEntity -> it.clone()
      is WaitActionEntity -> it.clone()
      else -> null
    } }.toMutableList()

    val fetchAction = newSource.actions.filterIsInstance<FetchActionEntity>().first()
    fetchAction.url = url

    return newSource
  }

  private fun processPlugins(corrId: String, documentId: UUID, jobs: List<DocumentPipelineJobEntity>) {
    log.info("[$corrId] ${jobs.size} processPlugins for webDocument $documentId")
    val document = documentDAO.findById(documentId).orElseThrow()
    val repository = repositoryDAO.findById(document.repositoryId).orElseThrow()
    try {
      var omitted = false
      for (job in jobs) {
        try {
          if (job.attempt > 3) {
            throw IllegalArgumentException("max attempts reached")
          }

          when (val plugin = pluginService.resolveById<FeedlessPlugin>(job.executorId)) {
            is FilterEntityPlugin -> if (!plugin.filterEntity(corrId, document, job.executorParams, 0)) {
              omitted = true
              break
            }

            is MapEntityPlugin -> plugin.mapEntity(corrId, document, repository, job.executorParams)
            else -> throw IllegalArgumentException("Invalid executorId ${job.executorId}")
          }
          job.status = PipelineJobStatus.SUCCEEDED
          job.updateStatus()
          documentPipelineJobDAO.save(job)

        } catch (e: ResumableHarvestException) {
          log.info("[$corrId] delaying (${job.executorId}): ${e.message}")
          job.coolDownUntil = Date(System.currentTimeMillis() + e.nextRetryAfter.toMillis())
          job.attempt += 1
          documentDAO.save(document)
          documentPipelineJobDAO.save(job)
          break
        }
      }
      if (omitted) {
        documentDAO.delete(document)
      } else {
        forwardToMail(corrId, document, repository)
        document.status = ReleaseStatus.released
        documentDAO.save(document)
        documentService.applyRetentionStrategy(corrId, repository)
      }

    } catch (throwable: Throwable) {
      log.warn("[$corrId] aborting pipeline, cause ${throwable.message}")
      documentDAO.delete(document)
    }
  }

  private fun forwardToMail(corrId: String, document: DocumentEntity, repository: RepositoryEntity) {
    val mailForwards = mailForwardDAO.findAllByRepositoryId(repository.id)
    if (mailForwards.isNotEmpty()) {
      val authorizedMailForwards =
        mailForwards.filterTo(ArrayList()) { it: MailForwardEntity -> it.authorized }.map { it.email }
      if (authorizedMailForwards.isEmpty()) {
        log.warn("[$corrId] no authorized mail-forwards available of ${mailForwards.size}")
      } else {
        val (mailFormatter, params) = pluginService.resolveMailFormatter(repository)
        log.info("[$corrId] using formatter ${mailFormatter::class.java.name}")

        val from = mailService.getNoReplyAddress(repository.product)
        val to = authorizedMailForwards.toTypedArray()

        log.info("[$corrId] resolved mail recipients [${authorizedMailForwards.joinToString(", ")}]")
        mailService.send(
          corrId,
          from,
          to,
          mailFormatter.provideDocumentMail(corrId, document, repository, params)
        )
      }
    }
  }

}

private fun SourceEntity.clone(): SourceEntity {
  val s = SourceEntity()
  BeanUtils.copyProperties(this, s, "actions", "repository")
  return s
}

private fun WaitActionEntity.clone(): WaitActionEntity {
  val e = WaitActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun HeaderActionEntity.clone(): HeaderActionEntity {
  val e = HeaderActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ExtractXpathActionEntity.clone(): ExtractXpathActionEntity {
  val e = ExtractXpathActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ExtractBoundingBoxActionEntity.clone(): ExtractBoundingBoxActionEntity {
  val e = ExtractBoundingBoxActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun DomActionEntity.clone(): DomActionEntity {
  val e = DomActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ClickXpathActionEntity.clone(): ClickXpathActionEntity {
  val e = ClickXpathActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ClickPositionActionEntity.clone(): ClickPositionActionEntity {
  val e = ClickPositionActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun ExecuteActionEntity.clone(): ExecuteActionEntity {
  val e = ExecuteActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

private fun FetchActionEntity.clone(): FetchActionEntity {
  val e = FetchActionEntity()
  BeanUtils.copyProperties(this, e, "source")
  return e
}

