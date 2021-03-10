package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.model.Source

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
