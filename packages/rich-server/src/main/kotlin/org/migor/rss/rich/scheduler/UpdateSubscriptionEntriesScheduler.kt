package org.migor.rss.rich.scheduler

import org.migor.rss.rich.filter.EntryFilter
import org.migor.rss.rich.filter.FilterOperator
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.model.SubscriptionEntry
import org.migor.rss.rich.repository.SourceEntryRepository
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
  lateinit var entryRepository: SourceEntryRepository

  @Autowired
  lateinit var subscriptionEntryRepository: SubscriptionEntryRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Scheduled(fixedDelay = 4567)
  fun releaseEntries() {
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextEntryReleaseAt")))
    subscriptionRepository.findDueToManagedSubscription(Date(), pageable)
      .forEach { subscription: Subscription -> releaseEntriesForSubscription(subscription) }
  }

  fun releaseEntriesForSubscription(subscription: Subscription): Subscription {
    try {
      val entries = entryRepository.findAllUnlinkedEntriesBySubscriptionId(subscription.id!!, EntryStatus.RELEASED)

      if (entries.isNotEmpty()) {
        linkEntriesToSubscription(createReleaseBatch(applyFilters(entries, subscription), subscription), subscription)
        subscriptionService.updateUpdatedAt(subscription)
      }

    } catch (e: Exception) {
      log.error("Cannot release entries for subscription ${subscription.id}")
      e.printStackTrace()
    } finally {
      // todo mag this is wrong
      subscriptionService.updateEntryReleaseDate(subscription)
    }
    return subscription
  }

  private fun applyFilters(entries: List<SourceEntry>, subscription: Subscription): List<SourceEntry> {
    return if (subscription.filtered) {
      entries.filter { sourceEntry: SourceEntry ->
        subscription.filters!!.stream().allMatch { filter: EntryFilter? ->
          applyIncludeExlude(filter,
            when (filter!!.operator) {
              FilterOperator.CONTAINS -> sourceEntry.content!![filter.fieldName].toString().contains(filter.value)
              else -> false
            })
        }
      }
    } else {
      entries
    }
  }

  private fun applyIncludeExlude(filter: EntryFilter?, matches: Boolean): Boolean {
    return (filter!!.include && matches) || !matches
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

