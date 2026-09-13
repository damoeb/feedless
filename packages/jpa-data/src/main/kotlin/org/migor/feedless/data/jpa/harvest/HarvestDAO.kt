package org.migor.feedless.data.jpa.harvest

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

// todo no repository

@Repository
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
interface HarvestDAO : JpaRepository<HarvestEntity, UUID> {
  // createdAt can tie; the id tiebreaker keeps pagination stable.
  @Query(
    """SELECT h FROM HarvestEntity h
    WHERE h.sourceId = :sourceId AND h.dryRun = :dryRun
    ORDER BY h.createdAt DESC, h.id ASC"""
  )
  fun findAllBySourceIdAndDryRun(
    @Param("sourceId") sourceId: UUID,
    @Param("dryRun") dryRun: Boolean,
    pageable: Pageable,
  ): List<HarvestEntity>

  @Modifying
  @Query(
    """
    WITH ranked_entities AS (
    SELECT
        id,
        source_id,
        ROW_NUMBER() OVER (PARTITION BY source_id, dry_run ORDER BY created_at DESC) AS row_num
    FROM
        t_harvest
    WHERE status = 'completed'
)
DELETE FROM t_harvest WHERE EXISTS(
    SELECT 1 FROM ranked_entities
    where row_num > 4 and t_harvest.id = ranked_entities.id
)
  """, nativeQuery = true
  )
  fun deleteAllTailingBySourceId()

  fun deleteAllByDryRunTrueAndStatusAndCreatedAtBefore(status: String, before: LocalDateTime)

  /**
   * SKIP LOCKED gives concurrent claimers disjoint sets. A real run is claimable only with no running or older queued real run
   * of its source, so real runs never overlap (V92's unique index) and start in queue order.
   */
  @Query(
    """
    SELECT h.* FROM t_harvest h
    WHERE h.status = 'queued'
      AND (h.dry_run OR NOT EXISTS (
        SELECT 1 FROM t_harvest o
        WHERE o.source_id = h.source_id
          AND o.dry_run = false
          AND (o.status = 'running'
            OR (o.status = 'queued' AND (o.created_at, o.id) < (h.created_at, h.id)))
      ))
    ORDER BY h.created_at ASC, h.id ASC
    LIMIT :limit
    FOR UPDATE OF h SKIP LOCKED
  """, nativeQuery = true
  )
  fun findQueuedForUpdateSkipLocked(@Param("limit") limit: Int): List<HarvestEntity>

  /** V92's partial unique index plus ON CONFLICT DO NOTHING: 0 rows when a real harvest already runs. */
  @Modifying
  @Query(
    """
    INSERT INTO t_harvest (id, created_at, errornous, items_added, items_ignored, logs, started_at, source_id, status, dry_run)
    VALUES (:id, :startedAt, false, 0, 0, '', :startedAt, :sourceId, 'running', false)
    ON CONFLICT DO NOTHING
  """, nativeQuery = true
  )
  fun insertRunningUnlessSourceRuns(
    @Param("id") id: UUID,
    @Param("sourceId") sourceId: UUID,
    @Param("startedAt") startedAt: LocalDateTime,
  ): Int

  @Modifying
  @Query(
    """
    UPDATE t_harvest
    SET status = 'completed',
        errornous = true,
        finished_at = :now,
        logs = concat_ws(E'\n', nullif(logs, ''), :message)
    WHERE status = 'running' AND started_at < :startedBefore
  """, nativeQuery = true
  )
  fun completeAllRunningStartedBefore(
    @Param("startedBefore") startedBefore: LocalDateTime,
    @Param("now") now: LocalDateTime,
    @Param("message") message: String,
  ): Int
}
