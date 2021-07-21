package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.SourceEntryDto
import org.migor.rss.rich.database.model.EntryStatus
import org.migor.rss.rich.database.model.SourceEntry
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.database.repository.SourceEntryRepository
import org.migor.rss.rich.database.repository.SubscriptionRepository
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

}
