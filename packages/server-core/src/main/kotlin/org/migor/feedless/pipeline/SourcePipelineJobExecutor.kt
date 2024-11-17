package org.migor.feedless.pipeline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.corrId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
@Transactional(propagation = Propagation.NEVER)
class SourcePipelineJobExecutor internal constructor(
  val sourceDAO: SourceDAO,
  val sourcePipelineJobDAO: SourcePipelineJobDAO,
  val repositoryDAO: RepositoryDAO,
  val sourceService: SourceService
) {

  private val log = LoggerFactory.getLogger(SourcePipelineJobExecutor::class.simpleName)

  private fun incrementSourceJobAttemptCount(groupedSources: Map<UUID, List<SourcePipelineJobEntity>>) {
    sourcePipelineJobDAO.incrementAttemptCount(groupedSources.keys.distinct())
  }

  @Scheduled(fixedDelay = 3245, initialDelay = 20000)
  @Transactional
  fun processSourceJobs() {
    try {
      val corrId = newCorrId()
      val groupedSources = sourcePipelineJobDAO.findAllPendingBatched(LocalDateTime.now())
        .groupBy { it.sourceId }

      incrementSourceJobAttemptCount(groupedSources)

      if (groupedSources.isNotEmpty()) {
        val semaphore = Semaphore(5)
        runBlocking {
          runCatching {
            coroutineScope {
              groupedSources.map { groupedSources ->
                try {
                  val userId = getOwnerIdForSourceId(groupedSources.key)
                  async(RequestContext(userId = userId)) {
                    semaphore.acquire()
                    delay(300)
                    try {
                      processSourcePipeline(groupedSources.key, groupedSources.value)
                    } finally {
                      semaphore.release()
                    }
                  }
                } catch (e: Exception) {
                  async {}
                }
              }.awaitAll()
            }
            log.info("[$corrId] done")
          }.onFailure {
            log.error("[$corrId] batch refresh done: ${it.message}", it)
          }
        }
      }
    } catch (e: Exception) {
      log.error(e.message)
    }
  }

  private suspend fun getOwnerIdForSourceId(sourceId: UUID): UUID {
    val repo = withContext(Dispatchers.IO) {
      repositoryDAO.findBySourceId(sourceId) ?: throw failAfterCleaningJobsForSource(sourceId)
    }
    return repo.ownerId
  }

  private suspend fun failAfterCleaningJobsForSource(sourceId: UUID): IllegalArgumentException {
    withContext(Dispatchers.IO) {
      try {
        sourcePipelineJobDAO.deleteBySourceId(sourceId)
      } catch (e: Exception) {
        log.warn("job cleanup of source $sourceId failed: ${e.message}")
      }
    }
    return IllegalArgumentException("repo not found by sourceId=$sourceId")
  }


  private suspend fun processSourcePipeline(sourceId: UUID, jobs: List<SourcePipelineJobEntity>) {
    val corrId = coroutineContext.corrId()
    try {
      sourceService.processSourcePipeline(sourceId, jobs)
    } catch (t: Throwable) {
      if (t !is ResumableHarvestException) {
        log.error("[$corrId] processDocumentPlugins fatal failure", t)
        withContext(Dispatchers.IO) {
          sourceDAO.setErrorState(sourceId, true, t.message)
        }
      }
    }
  }
}
