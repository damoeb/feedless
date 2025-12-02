package org.migor.feedless.harvest

import org.migor.feedless.PageableRequest
import org.migor.feedless.source.SourceId

interface HarvestRepository {
  fun findAllBySourceId(sourceId: SourceId, pageable: PageableRequest): List<Harvest>
  fun deleteAllTailingBySourceId()
  fun save(harvest: Harvest): Harvest
}
