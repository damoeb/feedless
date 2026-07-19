package org.migor.feedless.harvest

import org.migor.feedless.source.SourceId

interface HarvestUseCasePort {
  suspend fun findAllBySourceId(sourceId: SourceId, page: Int, pageSize: Int): List<Harvest>
}
