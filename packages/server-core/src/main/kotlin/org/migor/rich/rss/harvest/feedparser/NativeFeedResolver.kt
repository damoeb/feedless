package org.migor.rich.rss.harvest.feedparser

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichtFeed
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.harvest.HarvestContext
import org.springframework.stereotype.Service

@Service
class NativeFeedResolver : FeedContextResolver {

  override fun priority(): Int {
    return 0
  }

  override fun canHarvest(feed: Feed): Boolean {
    return true
  }

  override fun getHarvestContexts(corrId: String, feed: Feed): List<HarvestContext> {
    return listOf(
      HarvestContext(feed.feedUrl!!, feed),
    )
  }

  override fun mergeFeeds(feeds: List<RichtFeed>): List<Pair<RichArticle, Article>> {
    // todo mag whats this?
    return feeds.first().items.map { Pair(it, Article()) }
  }
}
