package org.migor.feedless.harvest

import org.migor.feedless.source.SourceId

interface HarvestUseCasePort {
  suspend fun findAllBySourceId(sourceId: SourceId, dryRun: Boolean, page: Int, pageSize: Int): List<Harvest>
  suspend fun findById(id: HarvestId): Harvest?
}
