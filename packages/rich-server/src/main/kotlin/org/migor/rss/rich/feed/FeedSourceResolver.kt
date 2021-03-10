package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.model.Source

interface FeedSourceResolver {
  fun canHandle(source: Source): Boolean
  fun feedUrls(source: Source): List<HarvestUrl>
}
