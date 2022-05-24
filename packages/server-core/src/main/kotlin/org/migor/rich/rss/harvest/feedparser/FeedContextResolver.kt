package org.migor.rich.rss.harvest.feedparser

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.harvest.HarvestContext

interface FeedContextResolver {
  fun priority(): Int
  fun canHarvest(feed: Feed): Boolean
  fun getHarvestContexts(corrId: String, feed: Feed): List<HarvestContext>
  fun mergeFeeds(syndFeeds: List<SyndFeed>): List<Pair<SyndEntry, Article>>
}
