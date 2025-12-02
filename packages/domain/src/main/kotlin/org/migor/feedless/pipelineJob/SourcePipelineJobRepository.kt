package org.migor.feedless.pipelineJob

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

interface SourcePipelineJobRepository {

  fun findAllPendingBatched(now: LocalDateTime): List<SourcePipelineJob>
  fun deleteAllByCreatedAtBefore(date: LocalDateTime)
  fun existsBySourceIdAndUrl(sourceId: SourceId, url: String): Boolean
  fun deleteBySourceId(sourceId: SourceId)
  fun incrementAttemptCount(jobIds: List<PipelineJobId>)
  fun save(job: SourcePipelineJob): SourcePipelineJob
  fun saveAll(jobs: List<SourcePipelineJob>): List<SourcePipelineJob>
}
