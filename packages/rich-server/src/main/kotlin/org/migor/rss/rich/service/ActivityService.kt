package org.migor.rss.rich.service

import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.repository.ActivityRepository
import org.migor.rss.rich.repository.FeedRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

@Service
class ActivityService {

  @Autowired
  lateinit var entryRepository: ActivityRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  //  fun findAllByUserId(userId: String): List<SourceEntryDto?> {
//    val entries = ArrayList<SourceEntry>()
//
//    val feeds = feedRepository.findAllByOwnerId(userId)
//    val publicFeed = feeds.find { feed -> "sink".equals(feed.name) }!!
//
//    entries.addAll(entryRepository.findLatestTransitiveEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))
//    entries.addAll(entryRepository.findLatestDirectEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))
//
//    // todo mag add activity if current user is allowed
////    if (!publicFeed.id.equals(feedId)) {
////      entries.addAll(entryRepository.findDirectEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))
////    }
//
//    return entries.stream()
//      .sorted(Comparator.comparing(SourceEntry::pubDate).reversed())
//      .map { entry: SourceEntry? -> entry!!.toDto() }
//      .collect(Collectors.toList())
//  }
//
  fun findLatestActivityBySubscriptionId(subscriptionId: String): Map<LocalDate, Int> {
    val subscription = subscriptionRepository.findById(subscriptionId).orElseThrow { RuntimeException("subscription $subscriptionId does not exit") }

    val latestPubDate = Date.from(LocalDateTime.now().minus(4, ChronoUnit.WEEKS).atZone(ZoneId.systemDefault()).toInstant())

    val dates = if (subscription.managed) {
      entryRepository.findLatestDirectEntriesBySubscriptionId(subscriptionId, EntryStatus.RELEASED, latestPubDate)
    } else {
      entryRepository.findLatestTransitiveEntriesBySubscriptionId(subscriptionId, EntryStatus.RELEASED, latestPubDate)
    }

    return dates.groupingBy { date -> date.toLocalDateTime().toLocalDate() }.eachCount()
  }

  fun findLatestActivityBySubscriptionGroupId(groupId: String): Map<LocalDate, Int> {
    val dates = ArrayList<Timestamp>()

    val latestPubDate = Date.from(LocalDateTime.now().minus(4, ChronoUnit.WEEKS).atZone(ZoneId.systemDefault()).toInstant())
    dates.addAll(entryRepository.findLatestDirectEntriesBySubscriptionGroupId(groupId, EntryStatus.RELEASED, latestPubDate))
    dates.addAll(entryRepository.findLatestTransitiveEntriesBySubscriptionGroupId(groupId, EntryStatus.RELEASED, latestPubDate))

    return dates.groupingBy { date -> date.toLocalDateTime().toLocalDate() }.eachCount()

  }

  fun findLatestActivityBySourceId(sourceId: String): Map<LocalDate, Int> {
    val latestPubDate = Date.from(LocalDateTime.now().minus(4, ChronoUnit.WEEKS).atZone(ZoneId.systemDefault()).toInstant())

    return entryRepository.findAllBySourceIdAndStatus(sourceId, EntryStatus.RELEASED, latestPubDate)
      .groupingBy { date -> date.toLocalDateTime().toLocalDate() }.eachCount()
  }
}
