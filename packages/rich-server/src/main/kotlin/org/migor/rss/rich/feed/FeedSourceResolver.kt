package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.model.SourceType
import org.migor.rss.rich.model.Subscription

interface FeedSourceResolver {
  fun canHandle(sourceType: SourceType): Boolean
  fun feedUrls(subscription: Subscription): List<HarvestUrl>
}
