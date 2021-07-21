package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.Subscription
import org.migor.rss.rich.database.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class SubscriptionService {

  private val log = LoggerFactory.getLogger(SubscriptionService::class.simpleName)

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Transactional
  fun updateEntryReleaseDate(subscription: Subscription) {
    val nextEntryReleaseAt = if (subscription.throttled) {
      Date.from(Date().toInstant().plus(Duration.of(subscription.releaseInterval!!, subscription.releaseTimeUnit)))
    } else {
      Date.from(Date().toInstant().plus(Duration.of(2, ChronoUnit.HOURS)))
    }
    log.debug("Scheduling next-entry-release for ${subscription.id} to $nextEntryReleaseAt")
    subscriptionRepository.updateNextEntryReleaseAt(subscription.id!!, nextEntryReleaseAt)
  }

  @Transactional
  fun updateUpdatedAt(subscription: Subscription) {
    subscriptionRepository.updateUpdatedAt(subscription.id!!, Date())
  }

}
