package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.jpa.repository.HarvestDAO
import org.migor.feedless.jpa.repository.HarvestEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class HarvestService(
  private var harvestDAO: HarvestDAO,
) {

  private val log = LoggerFactory.getLogger(HarvestService::class.simpleName)

  @Transactional
  suspend fun saveLast(data: HarvestEntity) {
    withContext(Dispatchers.IO) {
      harvestDAO.save(data)
    }
  }

  @Transactional
  fun deleteAllTailing() {
    harvestDAO.deleteAllTailingBySourceId()
  }

  @Transactional(readOnly = true)
  fun lastHarvests(sourceId: UUID): List<HarvestEntity> {
    return harvestDAO.findAllBySourceId(sourceId, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")))
  }
}
