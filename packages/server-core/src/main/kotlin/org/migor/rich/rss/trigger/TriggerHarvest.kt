package org.migor.rich.rss.trigger

import org.migor.rich.rss.database.repositories.HarvestTaskDAO
import org.migor.rich.rss.service.HarvestTaskService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database")
@Transactional(propagation = Propagation.NEVER)
class TriggerHarvest internal constructor() {

  private val log = LoggerFactory.getLogger(TriggerHarvest::class.simpleName)

  @Autowired
  lateinit var harvestTaskDAO: HarvestTaskDAO

  @Autowired
  lateinit var harvestTaskService: HarvestTaskService

  @Scheduled(fixedDelay = 3245)
  @Transactional(readOnly = true)
  fun harvestArticles() {
    val pageable = PageRequest.ofSize(20)
    harvestTaskDAO.findSomePending(Date(), pageable)
      .forEach { harvestTaskService.harvest(newCorrId(), it) }
  }
}
