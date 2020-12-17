package org.migor.rss.rich.scheduler

import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.migor.rss.rich.service.SubscriptionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*


@Component
class EntryReleaseScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(EntryReleaseScheduler::class.simpleName)

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @Autowired
  lateinit var entryRepository: EntryRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Scheduled(fixedDelay = 36789, initialDelay = 20000)
  fun releaseEntries() {
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextEntryReleaseAt")))
    subscriptionRepository.saveAll(subscriptionRepository.findAllByNextEntryReleaseAtBeforeAndThrottledIsTrue(Date(), pageable)
      .map { subscription: Subscription -> releaseEntriesForSubscription(subscription) })
  }

  fun releaseEntriesForSubscription(subscription: Subscription): Subscription {
    try {
      log.info("Releasing entries for ${subscription.id}")
      val batchSize = subscription.releaseBatchSize
      val pageable = PageRequest.of(0, batchSize!!, Sort.by(Sort.Order.desc("score")))
      entryRepository.findAllBySubscriptionIdAndStatusEquals(subscription.id!!, EntryStatus.TRANSFORMED, pageable)
        .forEach { entry -> releaseEntry(entry) }

    } catch (e: Exception) {
      log.error("Cannot release entries for subscription ${subscription.id}")
      e.printStackTrace()
    } finally {
      subscriptionService.updateEntryReleaseDate(subscription)
    }
    return subscription
  }

  private fun releaseEntry(entry: Entry) {
    log.info("Releasing ${entry.id}")
    entryRepository.updateStatus(entry.id!!, EntryStatus.RELEASED)
  }
}

