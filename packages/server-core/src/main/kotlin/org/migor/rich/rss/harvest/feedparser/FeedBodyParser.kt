package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestResponse
import org.springframework.util.MimeType

interface FeedBodyParser {
  fun priority(): Int
  fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean
  fun process(corrId: String, response: HarvestResponse): FeedData
}
