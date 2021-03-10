package org.migor.rss.rich.service

import org.migor.rss.rich.dto.SourceEntryDto
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.repository.FeedRepository
import org.migor.rss.rich.repository.SourceEntryRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

@Service
class EntryService {

  @Autowired
  lateinit var entryRepository: SourceEntryRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  fun findAllByUserId(userId: String): List<SourceEntryDto?> {
    val entries = ArrayList<SourceEntry>()

    val feeds = feedRepository.findAllByOwnerId(userId)
    val publicFeed = feeds.find { feed -> "sink".equals(feed.name) }!!

    entries.addAll(entryRepository.findLatestTransitiveEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))
    entries.addAll(entryRepository.findLatestDirectEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))

    // todo mag add activity if current user is allowed
//    if (!publicFeed.id.equals(feedId)) {
//      entries.addAll(entryRepository.findDirectEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))
//    }

    return entries.stream()
      .sorted(Comparator.comparing(SourceEntry::pubDate).reversed())
      .map { entry: SourceEntry? -> entry!!.toDto() }
      .collect(Collectors.toList())
  }

  fun findLatestBySubscriptionId(subscriptionId: String): List<SourceEntryDto?> {
    val pageable = PageRequest.of(0, 10)
    val subscription = subscriptionRepository.findById(subscriptionId).orElseThrow { RuntimeException("subscription $subscriptionId does not exit") }
    if (subscription.managed) {
      return entryRepository.findLatestDirectEntriesBySubscriptionId(subscriptionId, EntryStatus.RELEASED, pageable)
        .map { sourceEntry: SourceEntry -> sourceEntry.toDto() }
    } else {
      return entryRepository.findLatestTransitiveEntriesBySubscriptionId(subscriptionId, EntryStatus.RELEASED, pageable)
        .map { sourceEntry: SourceEntry -> sourceEntry.toDto() }
    }
  }

  fun findLatestBySubscriptionGroupId(groupId: String): List<SourceEntryDto?> {
    val pageable = PageRequest.of(0, 10)
    val entries = ArrayList<SourceEntry>()

    entries.addAll(entryRepository.findLatestDirectEntriesBySubscriptionGroupId(groupId, EntryStatus.RELEASED, pageable))
    entries.addAll(entryRepository.findLatestTransitiveEntriesBySubscriptionGroupId(groupId, EntryStatus.RELEASED, pageable))

    return entries.stream()
      .sorted(Comparator.comparing(SourceEntry::pubDate).reversed())
      .map { entry: SourceEntry -> entry.toDto() }
      .collect(Collectors.toList())
  }

  fun findLatestBySourceId(sourceId: String): List<SourceEntryDto?> {
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("pubDate")))
    return entryRepository.findAllBySourceIdAndStatus(sourceId, EntryStatus.RELEASED, pageable)
      .map { entry: SourceEntry -> entry.toDto() }
  }

  fun findById(entryId: String): SourceEntryDto? {
    return entryRepository.findById(entryId).orElseThrow { RuntimeException("entry $entryId does not exist") }.toDto()
  }

}
