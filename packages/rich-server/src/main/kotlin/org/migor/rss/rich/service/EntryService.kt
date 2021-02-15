package org.migor.rss.rich.service

import org.migor.rss.rich.dto.EntryDto
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.FeedRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

@Service
class EntryService {

  @Autowired
  lateinit var entryRepository: EntryRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  fun findAllByUserId(userId: String): List<EntryDto?> {
    val entries = ArrayList<SourceEntry>()

    val feeds = feedRepository.findAllByOwnerId(userId)
    val publicFeed = feeds.find { feed -> "public".equals(feed.name) }!!

    entries.addAll(entryRepository.findTransitiveEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))
    entries.addAll(entryRepository.findDirectEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))

    // todo mag add private if current user is allowed
//    if (!publicFeed.id.equals(feedId)) {
//      entries.addAll(entryRepository.findDirectEntriesByFeedId(publicFeed.id!!, EntryStatus.RELEASED, PageRequest.of(0, 10)))
//    }

    return entries.stream()
      .sorted(Comparator.comparing(SourceEntry::createdAt))
      .map { entry: SourceEntry? -> entry!!.toDto() }
      .collect(Collectors.toList())
  }

  fun findAllBySubscriptionId(subscriptionId: String): Page<EntryDto> {
    TODO("Not yet implemented")
  }

}
