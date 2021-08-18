package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestResponse

interface FeedParser {
  fun canProcess(feedType: FeedType): Boolean
  fun process(response: HarvestResponse): FeedData
}
