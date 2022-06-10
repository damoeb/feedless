package org.migor.rich.rss.harvest.feedparser

import org.migor.rich.rss.api.dto.FeedJsonDto
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

  override fun process(corrId: String, response: HarvestResponse): FeedJsonDto {
    throw HarvestException("No parser found for ${response.response.contentType}")
  }
}
