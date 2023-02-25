package org.migor.rich.rss.harvest.feedparser

import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.harvest.HarvestException
import org.migor.rich.rss.harvest.HarvestResponse

class NullFeedParser : FeedBodyParser {

  override fun priority(): Int {
    return 0
  }

  override fun canProcess(feedType: FeedType): Boolean {
    return true
  }

  override fun process(corrId: String, response: HarvestResponse): RichFeed {
    throw HarvestException("No parser found for ${response.response.contentType}")
  }
}
