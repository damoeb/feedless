package org.migor.rich.rss.harvest.feedparser

import com.rometools.rome.feed.synd.SyndFeed
import org.migor.rich.rss.harvest.HarvestResponse
import org.springframework.util.MimeType

interface FeedBodyParser {
  fun priority(): Int
  fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean
  fun process(corrId: String, response: HarvestResponse): SyndFeed
}
