package org.migor.rss.rich.trigger

import org.migor.rss.rich.database.model.Subscription
import org.migor.rss.rich.database.repository.SubscriptionRepository
import org.migor.rss.rich.harvest.SubscriptionHarvester
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class TriggerSubscription internal constructor() {

  private val log = LoggerFactory.getLogger(TriggerSubscription::class.simpleName)

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var subscriptionHarvester: SubscriptionHarvester

  @Scheduled(fixedDelay = 2345)
  @Transactional(readOnly = true)
  fun fillBuckets() {
    subscriptionRepository.findDueToSubscriptions(Date())
      .forEach { subscription: Subscription -> subscriptionHarvester.processSubscription(subscription) }
  }

  //  @PostMapping("/triggers/update/feed/{feedId}", produces = ["application/json;charset=UTF-8"])
//  fun triggerUpdate(@PathVariable("feedId") feedId: String) {
//    return streamService.triggerUpdate(streamId);
//  }

}

