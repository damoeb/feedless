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
}
