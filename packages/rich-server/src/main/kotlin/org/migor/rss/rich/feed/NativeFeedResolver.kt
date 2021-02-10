package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceType

class NativeFeedResolver : FeedSourceResolver {
  override fun canHandle(sourceType: SourceType): Boolean {
    return sourceType == SourceType.NATIVE
  }

  override fun feedUrls(source: Source): List<HarvestUrl> {
    return listOf(
      HarvestUrl(source.url!!),
    )
  }
}
