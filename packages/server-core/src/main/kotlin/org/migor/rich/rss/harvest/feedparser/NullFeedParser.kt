package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.springframework.util.MimeType

class NullFeedParser : FeedBodyParser {

  override fun priority(): Int {
    return 0
  }

  override fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean {
    return true
  }

  override fun process(corrId: String, response: HarvestResponse): FeedData {
    throw HarvestException("No parser found for ${response.response.contentType}")
  }
}
