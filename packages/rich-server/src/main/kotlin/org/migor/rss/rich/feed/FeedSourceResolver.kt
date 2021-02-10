package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceType

interface FeedSourceResolver {
  fun canHandle(sourceType: SourceType): Boolean
  fun feedUrls(source: Source): List<HarvestUrl>
}
