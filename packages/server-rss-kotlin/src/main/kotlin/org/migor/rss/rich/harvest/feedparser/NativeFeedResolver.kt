package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.HarvestContext

class NativeFeedResolver : FeedContextResolver {

  override fun priority(): Int {
    return 0
  }

  override fun canHarvest(feed: Feed): Boolean {
    return true
  }

  override fun getHarvestContexts(feed: Feed): List<HarvestContext> {
    return listOf(
      HarvestContext(feed.feedUrl!!),
    )
  }
}
