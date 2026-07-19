package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestUseCasePort
import org.migor.feedless.source.SourceId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class HarvestService(
  private val harvestRepository: HarvestRepository,
) : HarvestUseCasePort {

  private val log = LoggerFactory.getLogger(HarvestService::class.simpleName)

  suspend fun lastHarvests(sourceId: SourceId): List<Harvest> = withContext(Dispatchers.IO) {
    harvestRepository.findAllBySourceId(
      sourceId,
      PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")).toPageableRequest()
    )
  }

  override suspend fun findAllBySourceId(sourceId: SourceId, page: Int, pageSize: Int): List<Harvest> =
    withContext(Dispatchers.IO) {
      harvestRepository.findAllBySourceId(
        sourceId,
        PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")).toPageableRequest()
      )
    }
}
