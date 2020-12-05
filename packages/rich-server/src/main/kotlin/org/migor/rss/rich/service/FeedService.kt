package org.migor.rss.rich.service

import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.harvest.RichFeed
import org.springframework.stereotype.Service
import org.migor.rss.rich.locate.FeedLocator
import org.migor.rss.rich.model.Feed
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.FeedRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

@Service
class FeedService {

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  fun discover(url: String): FeedDiscovery {
    return FeedDiscovery(feeds = FeedLocator.locate(url))
  }

  fun saveOrUpdateFeedForSubscription(richFeed: RichFeed, subscription: Subscription): Feed {
    val feed: Feed
    if (subscription.feed == null) {
      feed = Feed()
      subscription.feed = feed
    } else {
      feed = subscription.feed!!
    }
    feed.description = richFeed.feed.description
    feed.link = richFeed.feed.link
    feed.name = subscription.name
    feed.title = richFeed.feed.title

    feedRepository.save(feed)
    subscriptionRepository.save(subscription)
    return feed
  }

  fun list(): Page<FeedDto> {
    return feedRepository.findAll(PageRequest.of(0, 10))
      .map { feed: Feed? -> feed?.toDto()}
  }
}
