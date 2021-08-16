package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.HarvestUrl

interface FeedSourceResolver {
  fun canHandle(feed: Feed): Boolean
  fun feedUrls(feed: Feed): List<HarvestUrl>
}
