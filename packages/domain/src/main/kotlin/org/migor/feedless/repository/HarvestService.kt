package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestId
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.source.SourceId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class HarvestService(
  private val harvestRepository: HarvestRepository,
) {

  private val log = LoggerFactory.getLogger(HarvestService::class.simpleName)

  suspend fun lastHarvests(sourceId: SourceId): List<Harvest> = withContext(Dispatchers.IO) {
    harvestRepository.findAllBySourceId(
      sourceId,
      dryRun = false,
      PageRequest.of(0, 1).toPageableRequest()
    )
  }

  // Asks for one extra row for hasMore without shifting the offset.
  suspend fun findAllBySourceId(sourceId: SourceId, dryRun: Boolean, page: Int, pageSize: Int): List<Harvest> =
    withContext(Dispatchers.IO) {
      harvestRepository.findAllBySourceId(
        sourceId,
        dryRun = dryRun,
        PageableRequest.withExtraForHasMore(page, pageSize)
      )
    }

  suspend fun findById(id: HarvestId): Harvest? = withContext(Dispatchers.IO) {
    harvestRepository.findById(id)
  }

  suspend fun enqueue(sourceId: SourceId, dryRun: Boolean, flow: String?): Harvest =
    withContext(Dispatchers.IO) {
      harvestRepository.save(
        Harvest(
          sourceId = sourceId,
          logs = "",
          // Replaced by the claim time once the scheduler picks the harvest up.
          startedAt = LocalDateTime.now(),
          finishedAt = null,
          status = HarvestStatus.QUEUED,
          dryRun = dryRun,
          flow = flow,
        )
      )
    }
}
