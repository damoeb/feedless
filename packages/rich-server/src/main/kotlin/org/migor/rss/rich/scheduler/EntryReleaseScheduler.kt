package org.migor.rss.rich.scheduler

import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


@Component
class EntryReleaseScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(EntryReleaseScheduler::class.simpleName)

  @Autowired
  lateinit var entryRepository: EntryRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Scheduled(fixedDelay = 6789)
  fun releaseEntries() {
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextEntryReleaseAt")))
    subscriptionRepository.saveAll(subscriptionRepository.findAllByNextEntryReleaseAtBefore(Date(), pageable)
      .map { subscription: Subscription -> releaseSubscription(subscription) })
  }

  fun releaseSubscription(subscription: Subscription): Subscription {
    try {
      val batchSize = 10
      val pageable = PageRequest.of(0, batchSize, Sort.by(Sort.Order.desc("score")))
      val entries = entryRepository.findAllBySubscriptionIdAndStatusEquals(subscription.id!!, EntryStatus.TRANSFORMED, pageable)
        .map { entry -> releaseEntry(entry) }
      entryRepository.saveAll(entries)

    } catch (e: Exception) {
      log.error("Cannot release entries for subscription ${subscription.id}")
      e.printStackTrace()
    } finally {
      subscription.nextEntryReleaseAt = Date.from(Date().toInstant().plus(Duration.ofHours(subscription.entryReleaseIntervalHours!!)))
    }
    return subscription
  }

  private fun releaseEntry(entry: Entry): Entry {
    entry.status = EntryStatus.RELEASED
    return entry
  }
}

