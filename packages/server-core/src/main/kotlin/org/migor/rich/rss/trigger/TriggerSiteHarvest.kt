package org.migor.rich.rss.trigger

import org.migor.rich.rss.database.repositories.HarvestTaskDAO
import org.migor.rich.rss.service.HarvestTaskService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database")
class TriggerSiteHarvest internal constructor() {

  @Autowired
  lateinit var harvestTaskDAO: HarvestTaskDAO

  @Autowired
  lateinit var harvestTaskService: HarvestTaskService

  @Scheduled(fixedDelay = 3245)
  @Transactional(readOnly = false)
  fun harvestArticles() {
    val pageable = PageRequest.of(0, 10)
    harvestTaskDAO.findAllPending(Date(), pageable)
      .forEach { siteHarvest -> harvestTaskService.harvest(newCorrId(), siteHarvest) }
  }
}
