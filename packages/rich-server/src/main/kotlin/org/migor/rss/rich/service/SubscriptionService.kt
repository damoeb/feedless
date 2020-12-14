package org.migor.rss.rich.service

import org.migor.rss.rich.dto.EntryDto
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.FeedRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class SubscriptionService {
  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var entryRepository: EntryRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  private val FEED_SIZE = 20

  fun list(): Page<SubscriptionDto> {
    return subscriptionRepository.findAll(PageRequest.of(0, 10))
      .map { s: Subscription? -> s?.toDto() }
  }

  fun feed(subscriptionId: String): FeedDto {

    val subscription = subscriptionRepository.findById(subscriptionId).get()
    val feedSize = if (subscription.feedSize == null) {
      FEED_SIZE
    } else {
      subscription.feedSize as Number
    }
    val feed = feedRepository.findBySubscriptionId(subscriptionId).get()
    val pageable = PageRequest.of(0, feedSize.toInt(), Sort.by(Sort.Direction.DESC, "createdAt"))
    val entries = entryRepository.findAllBySubscriptionId(subscriptionId, pageable)
      .content
      .map { entry: Entry? -> entry?.toDto() }
    return feed.toDto(entries = entries)!!
  }

  fun entries(subscriptionId: String): Page<EntryDto> {
    return entryRepository.findAllBySubscriptionId(subscriptionId, PageRequest.of(0, 10))
      .map { entry: Entry? -> entry?.toDto() }
  }
}
