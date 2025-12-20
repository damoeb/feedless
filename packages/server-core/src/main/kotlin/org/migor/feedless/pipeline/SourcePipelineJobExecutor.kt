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
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCase
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SourcePipelineJobExecutor internal constructor(
  private val sourcePipelineService: SourcePipelineService,
  private val sourcePipelineJobRepository: SourcePipelineJobRepository,
  private val sourceUseCase: SourceUseCase,
  private val repositoryRepository: RepositoryRepository,
  private val sourceRepository: SourceRepository
) {

  private val log = LoggerFactory.getLogger(SourcePipelineJobExecutor::class.simpleName)

  @Scheduled(fixedDelay = 3245, initialDelay = 20000)
  @Transactional
  fun processSourceJobs() {
    try {
      val groupedSources = sourcePipelineJobRepository.findAllPendingBatched(LocalDateTime.now())
        .groupBy { it.sourceId }

      sourcePipelineJobRepository.incrementAttemptCount(groupedSources.values.flatMap { it.map { it.id } }.distinct())

      if (groupedSources.isNotEmpty()) {
        val semaphore = Semaphore(5)
        runBlocking {
          runCatching {
            coroutineScope {
              groupedSources.map { groupedSources ->
                try {
                  val (userId, groupId) = getOwnerIdsForSourceId(groupedSources.key)
                  async(RequestContext(userId = userId, groupId = groupId)) {
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
            log.info("done")
          }.onFailure {
            log.error("batch refresh done: ${it.message}", it)
          }
        }
      }
    } catch (e: Exception) {
      log.error(e.message, e)
    }
  }

  private suspend fun getOwnerIdsForSourceId(sourceId: SourceId): Pair<UserId, GroupId> = withContext(Dispatchers.IO) {
    val repo =
      repositoryRepository.findBySourceId(sourceId) ?: throw sourcePipelineService.failAfterCleaningJobsForSource(
        sourceId
      )
    Pair(repo.ownerId, repo.groupId)
  }

  private suspend fun processSourcePipeline(sourceId: SourceId, jobs: List<SourcePipelineJob>) {
    try {
      sourceUseCase.processSourcePipeline(sourceId, jobs)
    } catch (t: Throwable) {
      if (t !is ResumableHarvestException) {
        log.error("processDocumentPlugins fatal failure ${t.message}")
        sourceRepository.setErrorState(sourceId, true, t.message)
      }
    }
  }
}
