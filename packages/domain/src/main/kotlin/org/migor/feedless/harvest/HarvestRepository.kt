package org.migor.feedless.harvest

import org.migor.feedless.PageableRequest
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

interface HarvestRepository {
  fun findAllBySourceId(sourceId: SourceId, dryRun: Boolean, pageable: PageableRequest): List<Harvest>
  fun findById(id: HarvestId): Harvest?
  fun deleteAllTailingBySourceId()
  fun deleteAllDryRunByCreatedAtBefore(before: LocalDateTime)
  fun save(harvest: Harvest): Harvest

  /**
   * Records a real (non-dry) harvest of [sourceId] as running as of [now] and returns it — or returns
   * null, recording nothing, when a real harvest of that source is running already, in any process.
   * At most one real harvest per source runs at a time; completing the returned harvest (or the
   * stale sweep, [completeStaleRunning]) frees the source again. Dry runs neither take nor block it.
   */
  fun startRun(sourceId: SourceId, now: LocalDateTime): Harvest?

  /**
   * Claims up to [limit] queued harvests, oldest first, and marks them running as of [now] — in one
   * transaction. Rows another claimer holds are skipped rather than waited for, so concurrent
   * claimers (several scheduler instances) never get the same harvest.
   *
   * A real harvest is claimed only while no real harvest of its source runs and no older real one of
   * it is queued; until then it stays queued for a later call. Should a real harvest of a claimed
   * source start between the claim's lock and its commit ([startRun]), the database refuses the
   * claim: this throws, and every harvest of the call stays queued.
   */
  fun claimQueued(limit: Int, now: LocalDateTime): List<Harvest>

  /**
   * Completes as failed every harvest still running that started before [startedBefore] — its run
   * died with the process — appending [message] to its log. Returns how many were completed.
   */
  fun completeStaleRunning(startedBefore: LocalDateTime, now: LocalDateTime, message: String): Int
}
