package org.migor.rss.rich.service

import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.dto.SubscriptionDto
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.locate.FeedLocator
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.repository.SourceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class SourceService {

  @Autowired
  lateinit var sourceRepository: SourceRepository

  fun discover(url: String): FeedDiscovery {
    return FeedDiscovery(feeds = FeedLocator.locate(url))
  }

  fun enrichSourceWithFeedDetails(richFeed: RichFeed, source: Source) {
//    val feed = feedRepository.findBySubscriptionId(source.id!!).orElse(Feed())
    source.description = richFeed.feed.description
    source.title = richFeed.feed.title
    source.language = richFeed.feed.language
    source.copyright = richFeed.feed.copyright
//    source.pubDate = richFeed.feed.publishedDate
//    feed.source = source
//    feed.subscriptionId = source.id
    sourceRepository.save(source)
  }

  fun updateUpdatedAt(source: Source) {
    sourceRepository.updateUpdatedAt(source.id!!, Date())
  }

  fun findBySubscription(subscriptionId: String): SubscriptionDto {
    TODO("Not yet implemented")
  }

}
