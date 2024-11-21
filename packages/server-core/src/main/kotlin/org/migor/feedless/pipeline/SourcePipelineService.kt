package org.migor.feedless.pipeline

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.scheduler}")
class SourcePipelineService internal constructor(
  val sourcePipelineJobDAO: SourcePipelineJobDAO,
) {

  private val log = LoggerFactory.getLogger(SourcePipelineService::class.simpleName)

  @Transactional
  fun incrementSourceJobAttemptCount(groupedSources: Map<UUID, List<SourcePipelineJobEntity>>) {
    sourcePipelineJobDAO.incrementAttemptCount(groupedSources.keys.distinct())
  }

  @Transactional
  suspend fun failAfterCleaningJobsForSource(sourceId: UUID): IllegalArgumentException {
    withContext(Dispatchers.IO) {
      try {
        sourcePipelineJobDAO.deleteBySourceId(sourceId)
      } catch (e: Exception) {
        log.warn("job cleanup of source $sourceId failed: ${e.message}")
      }
    }
    return IllegalArgumentException("repo not found by sourceId=$sourceId")
  }

  @Transactional(readOnly = true)
  fun findAllPendingBatched(now: LocalDateTime): List<SourcePipelineJobEntity> {
    return sourcePipelineJobDAO.findAllPendingBatched(now)
  }

  @Transactional
  fun deleteAllByCreatedAtBefore(refDate: LocalDateTime) {
    sourcePipelineJobDAO.deleteAllByCreatedAtBefore(refDate)
  }

  @Transactional(readOnly = true)
  suspend fun existsBySourceIdAndUrl(id: UUID, url: String): Boolean {
    return withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.existsBySourceIdAndUrl(id, url)
    }
  }

  @Transactional
  suspend fun saveAll(jobs: List<SourcePipelineJobEntity>): List<SourcePipelineJobEntity> {
    return withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.saveAll(jobs)
    }
  }
}
