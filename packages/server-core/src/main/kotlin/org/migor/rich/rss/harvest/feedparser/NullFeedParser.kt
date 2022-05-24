package org.migor.rich.rss.harvest.feedparser

import com.rometools.rome.feed.synd.SyndFeed
import org.migor.rich.rss.harvest.HarvestException
import org.migor.rich.rss.harvest.HarvestResponse
import org.springframework.util.MimeType

class NullFeedParser : FeedBodyParser {

  override fun priority(): Int {
    return 0
  }

  override fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean {
    return true
  }

  override fun process(corrId: String, response: HarvestResponse): SyndFeed {
    throw HarvestException("No parser found for ${response.response.contentType}")
  }
}
