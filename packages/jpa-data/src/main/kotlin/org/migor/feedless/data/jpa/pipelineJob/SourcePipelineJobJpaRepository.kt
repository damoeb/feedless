package org.migor.feedless.data.jpa.pipelineJob

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.pipelineJob.PipelineJobId
import org.migor.feedless.pipelineJob.SourcePipelineJob
import org.migor.feedless.pipelineJob.SourcePipelineJobRepository
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
class SourcePipelineJobJpaRepository(private val sourcePipelineJobDAO: SourcePipelineJobDAO) :
  SourcePipelineJobRepository {
  override suspend fun findAllPendingBatched(now: LocalDateTime): List<SourcePipelineJob> {
    return withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.findAllPendingBatched(now).map { it.toDomain() }
    }
  }

  override suspend fun deleteAllByCreatedAtBefore(date: LocalDateTime) {
    withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.deleteAllByCreatedAtBefore(date)
    }
  }

  override suspend fun existsBySourceIdAndUrl(
    sourceId: SourceId,
    url: String
  ): Boolean {
    return withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.existsBySourceIdAndUrl(sourceId.uuid, url)
    }
  }

  override suspend fun deleteBySourceId(sourceId: SourceId) {
    withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.deleteBySourceId(sourceId.uuid)
    }
  }

  override suspend fun incrementAttemptCount(jobIds: List<PipelineJobId>) {
    withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.incrementAttemptCount(jobIds.map { it.uuid })
    }
  }

  override suspend fun save(job: SourcePipelineJob): SourcePipelineJob {
    return withContext(Dispatchers.IO) {
      sourcePipelineJobDAO.save(job.toEntity()).toDomain()
    }
  }

  override suspend fun saveAll(jobs: List<SourcePipelineJob>): List<SourcePipelineJob> {
    return sourcePipelineJobDAO.saveAll(jobs.map { it.toEntity() }).map { it.toDomain() }
  }

}
