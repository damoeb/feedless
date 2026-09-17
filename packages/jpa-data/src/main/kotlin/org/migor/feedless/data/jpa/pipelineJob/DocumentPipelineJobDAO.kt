package org.migor.feedless.data.jpa.pipelineJob

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.source.SourceHostSql
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
interface DocumentPipelineJobDAO : JpaRepository<DocumentPipelineJobEntity, UUID> {
  // A job waits for its harvest to complete: the harvest's final save would overwrite the plugin log lines.
  // Jobs on a cooling host would fail at once, and a host with strikes goes last, so bad hosts can't fill the batch.
  @Query(
    nativeQuery = true,
    value = """
      select p.* from t_pipeline_job p
      join t_document d on d.id = p.document_id
      left join t_host_cooldown c on c.host = ${SourceHostSql.DOCUMENT_EXPRESSION}
      where p.terminated = false
      and (c.blocked_until is null or c.blocked_until <= :now)
      and p.document_id in (
        select g.document_id
        from (
            select distinct on (document_id)
                    document_id, cool_down_until
            from t_pipeline_job
            where terminated = false AND document_id IS NOT NULL
            order by document_id, sequence_id
        ) g
      where g.cool_down_until is null
         or g.cool_down_until < :now)
      and not exists (
        select 1 from t_harvest h
        where h.id = p.harvest_id and h.status = 'running'
      )
      order by coalesce(c.strikes, 0), p.document_id, p.sequence_id
      limit 100
    """
  )
  fun findAllPendingBatched(@Param("now") now: LocalDateTime): List<DocumentPipelineJobEntity>
  fun deleteAllByCreatedAtBefore(date: LocalDateTime)

  fun deleteAllByDocumentIdIn(ids: List<UUID>)

  @Modifying
  @Query(
    """
    UPDATE DocumentPipelineJobEntity j
    SET j.attempt = j.attempt + 1,
        j.terminated = j.attempt > 4
    WHERE j.id in :jobIds
    """
  )
  fun incrementAttemptCount(@Param("jobIds") jobIds: List<UUID>)

}
