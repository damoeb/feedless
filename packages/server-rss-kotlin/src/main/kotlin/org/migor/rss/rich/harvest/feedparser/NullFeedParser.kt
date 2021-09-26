package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestResponse
import org.slf4j.LoggerFactory
import org.springframework.util.MimeType


class NullFeedParser : FeedBodyParser {

  private val log = LoggerFactory.getLogger(NullFeedParser::class.simpleName)

  override fun priority(): Int {
    return 0;
  }

  override fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean {
    return true
  }

  override fun process(response: HarvestResponse): FeedData {
    throw RuntimeException("No parser found for ${response.response.contentType}")
  }

}

