package org.migor.feedless.pipeline

import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.mail.MailForwardDAO
import org.migor.feedless.mail.MailForwardEntity
import org.migor.feedless.mail.MailService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
@Transactional(propagation = Propagation.NEVER)
class PluginsJob internal constructor() {

  private val log = LoggerFactory.getLogger(PluginsJob::class.simpleName)

  @Autowired
  lateinit var pipelineJobDAO: PipelineJobDAO

  @Autowired
  lateinit var documentDAO: DocumentDAO

  @Autowired
  lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  lateinit var pluginService: PluginService

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var mailForwardDAO: MailForwardDAO

  @Scheduled(fixedDelay = 3245, initialDelay = 20000)
  @Transactional
  fun executePlugins() {
    val corrId = newCorrId()
    pipelineJobDAO.findAllPendingBatched()
      .groupBy { it.documentId }
      .map { processPipelineJob(newCorrId(3, corrId), it.key, it.value) }
  }

  private fun processPipelineJob(corrId: String, documentId: UUID, jobs: List<PipelineJobEntity>) {
    log.info("[$corrId] ${jobs.size} processPipelineJobs for webDocument $documentId")
    val document = documentDAO.findById(documentId).orElseThrow()
    val repository = repositoryDAO.findById(document.repositoryId).orElseThrow()
    try {
      var omitted = false
      for (job in jobs) {
        try {
          if (job.attempt > 3) {
            throw IllegalArgumentException("max attempts reached")
          }

          val plugin = pluginService.resolveById<FeedlessPlugin>(job.executorId)
          when (plugin) {
            is FilterEntityPlugin -> if (!plugin.filterEntity(corrId, document, job.executorParams, 0)) {
              omitted = true
              break
            }

            is MapEntityPlugin -> plugin.mapEntity(corrId, document, repository, job.executorParams)
            else -> throw IllegalArgumentException("Invalid executorId ${job.executorId}")
          }
          job.terminated = true
          pipelineJobDAO.save(job)

        } catch (e: ResumableHarvestException) {
          log.info("[$corrId] delaying (${job.executorId}): ${e.message}")
          job.coolDownUntil = Date(System.currentTimeMillis() + e.nextRetryAfter.toMillis())
          job.attempt += 1
          documentDAO.save(document)
          pipelineJobDAO.save(job)
          break
        }
      }
      if (omitted) {
        documentDAO.delete(document)
      } else {
        forwardToMail(corrId, document, repository)
        document.status = ReleaseStatus.released
        documentDAO.save(document)
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
