package org.migor.rss.rich.service

import org.migor.rss.rich.dto.FeedDiscovery
import org.springframework.stereotype.Service
import org.migor.rss.rich.locate.FeedLocator

@Service
class FeedService {
  fun discover(url: String): FeedDiscovery {
    return FeedDiscovery(feeds = FeedLocator.locate(url))
  }
}
