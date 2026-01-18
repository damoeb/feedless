package org.migor.feedless.data.jpa.pipelineJob

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
  override fun findAllPendingBatched(now: LocalDateTime): List<DocumentPipelineJob> {
    return documentPipelineJobDAO.findAllPendingBatched(now).map { it.toDomain() }
  }

  override fun deleteAllByCreatedAtBefore(date: LocalDateTime) {
    documentPipelineJobDAO.deleteAllByCreatedAtBefore(date)
  }

  override fun deleteAllByDocumentIdIn(ids: List<DocumentId>) {
    documentPipelineJobDAO.deleteAllByDocumentIdIn(ids.map { it.uuid })
  }

  override fun incrementAttemptCount(jobIds: List<PipelineJobId>) {
    documentPipelineJobDAO.incrementAttemptCount(jobIds.map { it.uuid })
  }

  override fun save(job: DocumentPipelineJob): DocumentPipelineJob {
    return documentPipelineJobDAO.save(job.toEntity()).toDomain()
  }

  override fun saveAll(jobs: List<DocumentPipelineJob>): List<DocumentPipelineJob> {
    return documentPipelineJobDAO.saveAll(jobs.map { it.toEntity() }).map { it.toDomain() }
  }

}
