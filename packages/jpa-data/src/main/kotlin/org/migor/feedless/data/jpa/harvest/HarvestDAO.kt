package org.migor.feedless.data.jpa.harvest

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
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
  fun findAllBySourceIdAndDryRunOrderByCreatedAtDesc(
    sourceId: UUID,
    dryRun: Boolean,
    pageable: PageRequest
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
   * Locks the oldest queued harvests. `SKIP LOCKED` makes a concurrent claimer pass over rows this
   * transaction holds instead of waiting for them — and, once they are committed as `running`, the
   * `status` filter excludes them — so two claimers always get disjoint sets.
   */
  @Query(
    """
    SELECT * FROM t_harvest
    WHERE status = 'queued'
    ORDER BY created_at ASC
    LIMIT :limit
    FOR UPDATE SKIP LOCKED
  """, nativeQuery = true
  )
  fun findQueuedForUpdateSkipLocked(@Param("limit") limit: Int): List<HarvestEntity>

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
