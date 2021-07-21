package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.database.model.Source

interface FeedSourceResolver {
  fun canHandle(source: Source): Boolean
  fun feedUrls(source: Source): List<HarvestUrl>
}
