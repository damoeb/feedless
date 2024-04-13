package org.migor.feedless.harvest

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class TriggerSourceSubscription internal constructor() {

  @Autowired
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Autowired
  lateinit var sourceSubscriptionHarvester: SourceSubscriptionHarvester

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  @Transactional(readOnly = true)
  fun refreshSubscriptions() {
    val pageable = PageRequest.ofSize(10)
    val corrId = newCorrId()
    sourceSubscriptionDAO.findSomeDue(Date(), pageable)
      .forEach { sourceSubscriptionHarvester.handleSourceSubscription(corrId, it) }
  }
}
