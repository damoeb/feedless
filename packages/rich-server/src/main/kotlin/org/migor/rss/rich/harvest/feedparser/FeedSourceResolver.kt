package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.HarvestUrl

interface FeedSourceResolver {
  fun canHandle(source: Feed): Boolean
  fun feedUrls(source: Feed): List<HarvestUrl>
}
