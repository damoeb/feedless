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
   * Claims up to [limit] queued harvests, oldest first, and marks them running as of [now] — in one
   * transaction. Rows another claimer holds are skipped rather than waited for, so concurrent
   * claimers (several scheduler instances) never get the same harvest.
   */
  fun claimQueued(limit: Int, now: LocalDateTime): List<Harvest>

  /**
   * Completes as failed every harvest still running that started before [startedBefore] — its run
   * died with the process — appending [message] to its log. Returns how many were completed.
   */
  fun completeStaleRunning(startedBefore: LocalDateTime, now: LocalDateTime, message: String): Int
}
