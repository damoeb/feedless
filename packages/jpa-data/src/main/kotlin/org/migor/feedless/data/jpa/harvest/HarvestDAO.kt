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
  // createdAt alone is not unique (harvests can be created within the same millisecond), so a
  // trailing id tiebreaker keeps LIMIT/OFFSET pagination from repeating or dropping rows.
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
   * Locks the oldest claimable queued harvests. `SKIP LOCKED` makes a concurrent claimer pass over
   * rows this transaction holds instead of waiting for them — and, once they are committed as
   * `running`, the `status` filter excludes them — so two claimers always get disjoint sets.
   *
   * A real (non-dry) run is claimable only while no real run of its source is running and no older
   * real run of it is queued: a claim never starts a second real harvest of a source (V89's partial
   * unique index enforces that), and a source's real runs start in the order they were queued. A
   * concurrent claimer holding the older one locked makes the newer one unclaimable too.
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

  /**
   * Records a real harvest of [sourceId] as running, unless one is running already: V89's partial
   * unique index refuses the row, and `ON CONFLICT DO NOTHING` turns that into 0 rows inserted
   * instead of an error. An uncommitted conflicting row is waited for, so the answer is never a race.
   */
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
