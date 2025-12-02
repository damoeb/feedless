package org.migor.feedless.pipeline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.source.SourceId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SourcePipelineService internal constructor(
  val sourcePipelineJobRepository: SourcePipelineJobRepository,
) {

  private val log = LoggerFactory.getLogger(SourcePipelineService::class.simpleName)

  suspend fun incrementSourceJobAttemptCount(groupedSources: Map<SourceId, List<SourcePipelineJob>>) =
    withContext(Dispatchers.IO) {
      sourcePipelineJobRepository.incrementAttemptCount(groupedSources.values.flatMap { it.map { it.id } }.distinct())
    }

  suspend fun failAfterCleaningJobsForSource(sourceId: SourceId): IllegalArgumentException =
    withContext(Dispatchers.IO) {
      try {
        sourcePipelineJobRepository.deleteBySourceId(sourceId)
      } catch (e: Exception) {
        log.warn("job cleanup of source $sourceId failed: ${e.message}")
      }

      IllegalArgumentException("repo not found by sourceId=$sourceId")
    }

//  suspend fun findAllPendingBatched(now: LocalDateTime): List<SourcePipelineJob> = withContext(Dispatchers.IO) {
//    sourcePipelineJobRepository.findAllPendingBatched(now)
//  }
//
//  suspend fun deleteAllByCreatedAtBefore(refDate: LocalDateTime) = withContext(Dispatchers.IO) {
//    sourcePipelineJobRepository.deleteAllByCreatedAtBefore(refDate)
//  }
//
//  suspend fun existsBySourceIdAndUrl(id: SourceId, url: String): Boolean = withContext(Dispatchers.IO) {
//    sourcePipelineJobRepository.existsBySourceIdAndUrl(id, url)
//  }
//
//  suspend fun saveAll(jobs: List<SourcePipelineJob>): List<SourcePipelineJob> = withContext(Dispatchers.IO) {
//    sourcePipelineJobRepository.saveAll(jobs)
//  }
}
