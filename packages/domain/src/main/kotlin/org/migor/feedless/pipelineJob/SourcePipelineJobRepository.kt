package org.migor.feedless.pipelineJob

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

interface SourcePipelineJobRepository {

  suspend fun findAllPendingBatched(now: LocalDateTime): List<SourcePipelineJob>
  suspend fun deleteAllByCreatedAtBefore(date: LocalDateTime)
  suspend fun existsBySourceIdAndUrl(sourceId: SourceId, url: String): Boolean
  suspend fun deleteBySourceId(sourceId: SourceId)
  suspend fun incrementAttemptCount(jobIds: List<PipelineJobId>)
  suspend fun save(job: SourcePipelineJob): SourcePipelineJob
  suspend fun saveAll(jobs: List<SourcePipelineJob>): List<SourcePipelineJob>
}
