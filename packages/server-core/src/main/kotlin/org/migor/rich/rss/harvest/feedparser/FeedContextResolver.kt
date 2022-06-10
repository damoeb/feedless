package org.migor.rich.rss.harvest.feedparser

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichtFeed
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.harvest.HarvestContext

interface FeedContextResolver {
  fun priority(): Int
  fun canHarvest(feed: Feed): Boolean
  fun getHarvestContexts(corrId: String, feed: Feed): List<HarvestContext>
  fun mergeFeeds(feeds: List<RichtFeed>): List<Pair<RichArticle, Article>>
}
