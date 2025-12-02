package org.migor.feedless.pipelineJob

import org.migor.feedless.document.DocumentId
import java.time.LocalDateTime

interface DocumentPipelineJobRepository {
  fun findAllPendingBatched(now: LocalDateTime): List<DocumentPipelineJob>
  fun deleteAllByCreatedAtBefore(date: LocalDateTime)

  fun deleteAllByDocumentIdIn(ids: List<DocumentId>)
  fun incrementAttemptCount(jobIds: List<PipelineJobId>)
  fun save(job: DocumentPipelineJob): DocumentPipelineJob
  fun saveAll(jobs: List<DocumentPipelineJob>): List<DocumentPipelineJob>
}
