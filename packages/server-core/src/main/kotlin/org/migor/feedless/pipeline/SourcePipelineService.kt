package org.migor.feedless.pipeline

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.source.SourceId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SourcePipelineService internal constructor(
  val sourcePipelineJobRepository: SourcePipelineJobRepository,
) {

  private val log = LoggerFactory.getLogger(SourcePipelineService::class.simpleName)

  @Transactional
  suspend fun incrementSourceJobAttemptCount(groupedSources: Map<SourceId, List<SourcePipelineJob>>) {
    sourcePipelineJobRepository.incrementAttemptCount(groupedSources.values.flatMap { it.map { it.id } }.distinct())
  }

  @Transactional
  suspend fun failAfterCleaningJobsForSource(sourceId: SourceId): IllegalArgumentException {
    try {
      sourcePipelineJobRepository.deleteBySourceId(sourceId)
    } catch (e: Exception) {
      log.warn("job cleanup of source $sourceId failed: ${e.message}")
    }

    return IllegalArgumentException("repo not found by sourceId=$sourceId")
  }

  @Transactional(readOnly = true)
  suspend fun findAllPendingBatched(now: LocalDateTime): List<SourcePipelineJob> {
    return sourcePipelineJobRepository.findAllPendingBatched(now)
  }

  @Transactional
  suspend fun deleteAllByCreatedAtBefore(refDate: LocalDateTime) {
    sourcePipelineJobRepository.deleteAllByCreatedAtBefore(refDate)
  }

  @Transactional(readOnly = true)
  suspend fun existsBySourceIdAndUrl(id: SourceId, url: String): Boolean {
    return sourcePipelineJobRepository.existsBySourceIdAndUrl(id, url)
  }

  @Transactional
  suspend fun saveAll(jobs: List<SourcePipelineJob>): List<SourcePipelineJob> {
    return sourcePipelineJobRepository.saveAll(jobs)
  }
}
