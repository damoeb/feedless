package org.migor.rss.rich.service

import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.locate.FeedLocator
import org.migor.rss.rich.model.Feed
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.FeedRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class FeedService {

  @Autowired
  lateinit var feedRepository: FeedRepository

  fun discover(url: String): FeedDiscovery {
    return FeedDiscovery(feeds = FeedLocator.locate(url))
  }

  fun createFeedForSubscription(richFeed: RichFeed, subscription: Subscription) {
    val feed = feedRepository.findBySubscriptionId(subscription.id!!).orElse(Feed())
    feed.description = richFeed.feed.description
    feed.link = richFeed.feed.link
    feed.name = subscription.name
    feed.title = richFeed.feed.title
    feed.subscription = subscription
    feed.subscriptionId = subscription.id
    feedRepository.save(feed)
  }

  fun list(): Page<FeedDto> {
    return feedRepository.findAll(PageRequest.of(0, 10))
      .map { feed: Feed? -> feed?.toDto(null)}
  }


}
