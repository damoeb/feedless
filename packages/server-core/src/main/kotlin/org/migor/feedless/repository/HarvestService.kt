package org.migor.feedless.repository

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.source.SourceId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class HarvestService(
  private var harvestRepository: HarvestRepository,
) {

  private val log = LoggerFactory.getLogger(HarvestService::class.simpleName)

  @Transactional
  fun deleteAllTailing() {
    harvestRepository.deleteAllTailingBySourceId()
  }

  @Transactional(readOnly = true)
  fun lastHarvests(sourceId: SourceId): List<Harvest> {
    return harvestRepository.findAllBySourceId(
      sourceId,
      PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")).toPageableRequest()
    )
  }
}
