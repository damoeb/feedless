package org.migor.rich.rss.trigger

import org.migor.rich.rss.database.repositories.SiteHarvestDAO
import org.migor.rich.rss.service.SiteHarvestService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database2")
class TriggerSiteHarvest internal constructor() {

  @Autowired
  lateinit var siteHarvestDAO: SiteHarvestDAO

  @Autowired
  lateinit var siteHarvestService: SiteHarvestService

  @Scheduled(fixedDelay = 3245)
  @Transactional(readOnly = false)
  fun harvestArticles() {
    siteHarvestDAO.findAllPending(Date(), 2)
      .forEach { siteHarvest -> siteHarvestService.harvest(newCorrId(), siteHarvest) }
  }
}
