package org.migor.feedless.data.jpa.pipelineJob

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.pipelineJob.PipelineJobId
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
class SourcePipelineJobJpaRepository(private val sourcePipelineJobDAO: SourcePipelineJobDAO) :
  SourcePipelineJobRepository {
  override fun findAllPendingBatched(now: LocalDateTime): List<SourcePipelineJob> {
    return sourcePipelineJobDAO.findAllPendingBatched(now).map { it.toDomain() }
  }

  override fun deleteAllByCreatedAtBefore(date: LocalDateTime) {
    sourcePipelineJobDAO.deleteAllByCreatedAtBefore(date)
  }

  override fun existsBySourceIdAndUrl(
    sourceId: SourceId,
    url: String
  ): Boolean {
    return sourcePipelineJobDAO.existsBySourceIdAndUrl(sourceId.uuid, url)
  }

  override fun deleteBySourceId(sourceId: SourceId) {
    sourcePipelineJobDAO.deleteBySourceId(sourceId.uuid)
  }

  override fun incrementAttemptCount(jobIds: List<PipelineJobId>) {
    sourcePipelineJobDAO.incrementAttemptCount(jobIds.map { it.uuid })
  }

  override fun save(job: SourcePipelineJob): SourcePipelineJob {
    return sourcePipelineJobDAO.save(job.toEntity()).toDomain()
  }

  override fun saveAll(jobs: List<SourcePipelineJob>): List<SourcePipelineJob> {
    return sourcePipelineJobDAO.saveAll(jobs.map { it.toEntity() }).map { it.toDomain() }
  }

}
