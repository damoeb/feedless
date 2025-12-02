package org.migor.feedless.pipelineJob

import org.migor.feedless.document.DocumentId
import java.time.LocalDateTime

interface DocumentPipelineJobRepository {
  suspend fun findAllPendingBatched(now: LocalDateTime): List<DocumentPipelineJob>
  suspend fun deleteAllByCreatedAtBefore(date: LocalDateTime)

  suspend fun deleteAllByDocumentIdIn(ids: List<DocumentId>)
  suspend fun incrementAttemptCount(jobIds: List<PipelineJobId>)
  suspend fun save(job: DocumentPipelineJob): DocumentPipelineJob
  suspend fun saveAll(jobs: List<DocumentPipelineJob>): List<DocumentPipelineJob>
}
