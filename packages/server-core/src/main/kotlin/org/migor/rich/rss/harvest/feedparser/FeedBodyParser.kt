package org.migor.rich.rss.harvest.feedparser

import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.harvest.HarvestResponse

interface FeedBodyParser {
  fun priority(): Int
  fun canProcess(feedType: FeedType): Boolean
  fun process(corrId: String, response: HarvestResponse): RichFeed
}
