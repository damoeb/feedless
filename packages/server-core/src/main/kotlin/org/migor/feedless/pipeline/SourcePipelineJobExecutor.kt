package org.migor.feedless.pipeline

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.pipelineJob.toDomain
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SourcePipelineJobExecutor internal constructor(
    val sourcePipelineService: SourcePipelineService,
    val repositoryService: RepositoryService,
    val sourceService: SourceService
) {

    private val log = LoggerFactory.getLogger(SourcePipelineJobExecutor::class.simpleName)

    @Scheduled(fixedDelay = 3245, initialDelay = 20000)
    fun processSourceJobs() {
        try {
            val corrId = newCorrId()
            val groupedSources = sourcePipelineService.findAllPendingBatched(LocalDateTime.now())
                .map { it.toDomain() }
                .groupBy { it.sourceId }

            sourcePipelineService.incrementSourceJobAttemptCount(groupedSources)

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

    private suspend fun getOwnerIdForSourceId(sourceId: SourceId): UserId {
        val repo =
            repositoryService.findBySourceId(sourceId) ?: throw sourcePipelineService.failAfterCleaningJobsForSource(
                sourceId
            )
        return repo.ownerId
    }

    private suspend fun processSourcePipeline(sourceId: SourceId, jobs: List<SourcePipelineJob>) {
        val corrId = coroutineContext.corrId()
        try {
            sourceService.processSourcePipeline(sourceId, jobs)
        } catch (t: Throwable) {
            if (t !is ResumableHarvestException) {
                log.error("[$corrId] processDocumentPlugins fatal failure ${t.message}")
                sourceService.setErrorState(sourceId, true, t.message)
            }
        }
    }
}
