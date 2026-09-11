package org.migor.feedless.harvest

import org.migor.feedless.source.SourceId

interface HarvestUseCasePort {
  suspend fun findAllBySourceId(sourceId: SourceId, dryRun: Boolean, page: Int, pageSize: Int): List<Harvest>
  suspend fun findById(id: HarvestId): Harvest?

  /** [flow] overrides the saved flow, for a dry run only. */
  suspend fun enqueue(sourceId: SourceId, dryRun: Boolean, flow: String?): Harvest
}
