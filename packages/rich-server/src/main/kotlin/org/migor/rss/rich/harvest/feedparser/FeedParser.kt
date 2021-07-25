package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestResponse

interface FeedParser {
  fun canProcess(feedType: Pair<FeedType, String>): Boolean
  fun process(response: HarvestResponse): FeedData
}
