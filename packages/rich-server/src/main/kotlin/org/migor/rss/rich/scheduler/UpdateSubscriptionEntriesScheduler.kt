package org.migor.rss.rich.scheduler

import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.model.SubscriptionEntry
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.SubscriptionEntryRepository
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
class UpdateSubscriptionEntriesScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(UpdateSubscriptionEntriesScheduler::class.simpleName)

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @Autowired
  lateinit var entryRepository: EntryRepository

  @Autowired
  lateinit var subscriptionEntryRepository: SubscriptionEntryRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Scheduled(fixedDelay = 4567)
  fun releaseEntries() {
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextEntryReleaseAt")))
    subscriptionRepository.findDueToThrottledSubscription(Date(), pageable)
      .forEach { subscription: Subscription -> releaseEntriesForSubscription(subscription) }
  }

  fun releaseEntriesForSubscription(subscription: Subscription): Subscription {
    try {
      val entries = entryRepository.findAllNewEntriesBySubscriptionId(subscription.id!!, EntryStatus.RELEASED)

      if (!entries.isEmpty()) {
        linkEntriesToSubscription(createReleaseBatch(entries, subscription), subscription)
        subscriptionService.updateUpdatedAt(subscription)
      }

    } catch (e: Exception) {
      log.error("Cannot release entries for subscription ${subscription.id}")
      e.printStackTrace()
    } finally {
      // todo mag this is wrog
      subscriptionService.updateEntryReleaseDate(subscription)
    }
    return subscription
  }

  private fun createReleaseBatch(entries: List<SourceEntry>, subscription: Subscription): List<SourceEntry> {
    return if (subscription.throttled) {
      entries.subList(0, subscription.releaseBatchSize!!)
    } else {
      entries
    }
  }

  private fun linkEntriesToSubscription(entries: List<SourceEntry>, subscription: Subscription) {
    entries.forEach { entry: SourceEntry -> subscriptionEntryRepository.save(SubscriptionEntry(entry, subscription)) }
    log.info("Linking ${entries.size} entries to subscription ${subscription.id}")
  }
}

