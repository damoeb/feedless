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

  /** Returns null, recording nothing, when a real harvest of the source already runs in any process; dry runs don't count. */
  fun startRun(sourceId: SourceId, now: LocalDateTime): Harvest?

  /**
   * Claims queued harvests with SKIP LOCKED, so concurrent schedulers never get the same one.
   * A real harvest waits while its source has a running or older queued real harvest; racing [startRun] makes this throw.
   */
  fun claimQueued(limit: Int, now: LocalDateTime): List<Harvest>

  /** Fails running harvests started before [startedBefore]: their process died. */
  fun completeStaleRunning(startedBefore: LocalDateTime, now: LocalDateTime, message: String): Int
}
