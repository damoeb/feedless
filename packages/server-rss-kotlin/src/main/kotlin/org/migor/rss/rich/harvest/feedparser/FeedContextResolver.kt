package org.migor.rss.rich.harvest.feedparser

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestContext

interface FeedContextResolver {
  fun priority(): Int
  fun canHarvest(feed: Feed): Boolean
  fun getHarvestContexts(corrId: String, feed: Feed): List<HarvestContext>
  fun mergeFeeds(feedData: List<FeedData>): List<Pair<SyndEntry, Article>>
}
