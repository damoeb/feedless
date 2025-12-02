package org.migor.feedless.data.jpa.pipelineJob

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentId
import org.migor.feedless.pipelineJob.DocumentPipelineJob
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.PipelineJobId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
class DocumentPipelineJobJpaRepository(private val documentPipelineJobDAO: DocumentPipelineJobDAO) :
  DocumentPipelineJobRepository {
  override suspend fun findAllPendingBatched(now: LocalDateTime): List<DocumentPipelineJob> {
    return withContext(Dispatchers.IO) {
      documentPipelineJobDAO.findAllPendingBatched(now).map { it.toDomain() }
    }
  }

  override suspend fun deleteAllByCreatedAtBefore(date: LocalDateTime) {
    withContext(Dispatchers.IO) {
      documentPipelineJobDAO.deleteAllByCreatedAtBefore(date)
    }
  }

  override suspend fun deleteAllByDocumentIdIn(ids: List<DocumentId>) {
    withContext(Dispatchers.IO) {
      documentPipelineJobDAO.deleteAllByDocumentIdIn(ids.map { it.uuid })
    }
  }

  override suspend fun incrementAttemptCount(jobIds: List<PipelineJobId>) {
    withContext(Dispatchers.IO) {
      documentPipelineJobDAO.incrementAttemptCount(jobIds.map { it.uuid })
    }
  }

  override suspend fun save(job: DocumentPipelineJob): DocumentPipelineJob {
    return withContext(Dispatchers.IO) {
      documentPipelineJobDAO.save(job.toEntity()).toDomain()
    }
  }

  override suspend fun saveAll(jobs: List<DocumentPipelineJob>): List<DocumentPipelineJob> {
    return withContext(Dispatchers.IO) {
      documentPipelineJobDAO.saveAll(jobs.map { it.toEntity() }).map { it.toDomain() }
    }
  }

}
