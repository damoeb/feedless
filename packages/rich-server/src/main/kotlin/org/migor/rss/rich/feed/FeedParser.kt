package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.RichFeed

interface FeedParser {
  fun canProcess(response: HarvestResponse): Boolean
  fun process(response: HarvestResponse): RichFeed
}
