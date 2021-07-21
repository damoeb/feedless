package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.database.model.Source

class NativeFeedResolver : FeedSourceResolver {
  override fun canHandle(source: Source): Boolean {
    return true
  }

  override fun feedUrls(source: Source): List<HarvestUrl> {
    return listOf(
      HarvestUrl(source.url!!),
    )
  }
}
