package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.model.SourceType
import org.migor.rss.rich.model.Subscription

class NativeFeedResolver : FeedSourceResolver {
  override fun canHandle(sourceType: SourceType): Boolean {
    return sourceType == SourceType.NATIVE
  }

  override fun feedUrls(subscription: Subscription): List<HarvestUrl> {
    return listOf(
      HarvestUrl(subscription.url!!),
    )
  }
}
