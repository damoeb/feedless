package org.migor.feedless.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
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
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.pipeline.PipelineJobStatus
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobEntity
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.source} & ${AppLayer.service}")
@Transactional
class SourceService(
  private val sourcePipelineJobDAO: SourcePipelineJobDAO,
  private val sourceDAO: SourceDAO,
  private val repositoryHarvester: RepositoryHarvester,
  private val documentDAO: DocumentDAO
) {

  private val log = LoggerFactory.getLogger(SourceService::class.simpleName)

  suspend fun processSourcePipeline(sourceId: UUID, jobs: List<SourcePipelineJobEntity>) {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] ${jobs.size} processSourcePipeline for source $sourceId")

    val job = jobs.first()
    job.status = PipelineJobStatus.IN_PROGRESS

    val source = withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.save(job)
      sourceDAO.findByIdWithActions(sourceId)!!
    }

    try {
      try {
        repositoryHarvester.scrapeSource(patchRequestUrl(source, job.url), LogCollector())
        job.status = PipelineJobStatus.SUCCEEDED
        job.updateStatus()
        log.info("[$corrId] job ${job.id} done")
      } catch (e: ResumableHarvestException) {
        log.info("[$corrId] delaying: ${e.message}")
        job.coolDownUntil = LocalDateTime.now().plus(e.nextRetryAfter)
      }

    } catch (e: Exception) {
      log.warn("[$corrId] aborting scrape job, cause ${e.message}", e)
      job.status = PipelineJobStatus.FAILED
      job.updateStatus()
      job.logs = e.message
    }
    try {
      withContext(Dispatchers.IO) {
        sourcePipelineJobDAO.save(job)
      }
    } catch (e: Exception) {
      log.warn("[$corrId] ${e.message}]", e)
    }
  }

  private fun patchRequestUrl(source: SourceEntity, url: String): SourceEntity {
    val newSource = source.clone()
    newSource.actions = source.actions.mapNotNull {
      when (it) {
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
      }
    }.toMutableList()

    val fetchAction = newSource.actions.filterIsInstance<FetchActionEntity>().first()
    fetchAction.url = url

    return newSource
  }

  suspend fun findAllByRepositoryIdOrderByCreatedAtDesc(repositoryId: UUID): List<SourceEntity> {
    return withContext(Dispatchers.IO) {
      sourceDAO.findAllByRepositoryIdOrderByCreatedAtDesc(repositoryId)
    }
  }

  suspend fun existsByRepositoryIdAndDisabledTrue(repositoryId: UUID): Boolean {
    return withContext(Dispatchers.IO) {
      sourceDAO.existsByRepositoryIdAndDisabledTrue(repositoryId)
    }
  }

  suspend fun countDocumentsBySourceId(sourceId: UUID): Int {
    return withContext(Dispatchers.IO) {
      documentDAO.countBySourceId(sourceId)
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

