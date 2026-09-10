package org.migor.feedless.harvest

import org.migor.feedless.source.SourceId

interface HarvestUseCasePort {
  suspend fun findAllBySourceId(sourceId: SourceId, dryRun: Boolean, page: Int, pageSize: Int): List<Harvest>
  suspend fun findById(id: HarvestId): Harvest?

  /**
   * Queues a harvest of [sourceId] for the scheduler. [flow] is a stored override flow for a dry
   * run (see `HttpScrapeFlowMapper.toStoredFlow`); a real run always uses the saved flow.
   */
  suspend fun enqueue(sourceId: SourceId, dryRun: Boolean, flow: String?): Harvest
}
