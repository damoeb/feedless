package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.HarvestUrl

class NativeFeedResolver : FeedSourceResolver {
  override fun canHandle(feed: Feed): Boolean {
    return true
  }

  override fun feedUrls(feed: Feed): List<HarvestUrl> {
    return listOf(
      HarvestUrl(feed.feedUrl!!),
    )
  }
}
