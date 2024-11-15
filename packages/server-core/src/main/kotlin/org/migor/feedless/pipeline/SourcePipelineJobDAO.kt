package org.migor.feedless.pipeline

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
interface SourcePipelineJobDAO : JpaRepository<SourcePipelineJobEntity, UUID> {

  @Query(
    nativeQuery = true,
    value = """
      select p.* from t_pipeline_job p
      where p.terminated = false
      and p.source_id in (
        select g.source_id
        from (
            select distinct on (source_id)
                    source_id, cool_down_until
            from t_pipeline_job
            where terminated = false AND source_id IS NOT NULL
            order by source_id, sequence_id
        ) g
      where g.cool_down_until is null
         or g.cool_down_until < :now)
      order by source_id, sequence_id
      limit 100
    """
  )
  fun findAllPendingBatched(@Param("now") now: LocalDateTime): List<SourcePipelineJobEntity>
  fun deleteAllByCreatedAtBefore(date: LocalDateTime)
  fun existsBySourceIdAndUrl(sourceId: UUID, url: String): Boolean
  fun deleteBySourceId(sourceId: UUID)

  @Modifying
  @Query(
    """
    UPDATE SourcePipelineJobEntity j
    SET j.attempt = j.attempt + 1,
        j.terminated = j.attempt > 3
    WHERE j.id in :jobIds
    """
  )
  fun incrementAttemptCount(@Param("jobIds") jobIds: List<UUID>)
}
