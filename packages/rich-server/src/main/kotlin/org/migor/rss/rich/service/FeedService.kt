package org.migor.rss.rich.service

import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.harvest.RichFeed
import org.springframework.stereotype.Service
import org.migor.rss.rich.locate.FeedLocator
import org.migor.rss.rich.model.Subscription

@Service
class FeedService {
  fun discover(url: String): FeedDiscovery {
    return FeedDiscovery(feeds = FeedLocator.locate(url))
  }

  fun saveOrUpdateFeedForSubscription(richFeed: RichFeed, subscription: Subscription) {
    TODO("Not yet implemented")
  }
}
