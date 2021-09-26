package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.HarvestContext

interface FeedContextResolver {
  fun priority(): Int
  fun canHarvest(feed: Feed): Boolean
  fun getHarvestContexts(feed: Feed): List<HarvestContext>
}
