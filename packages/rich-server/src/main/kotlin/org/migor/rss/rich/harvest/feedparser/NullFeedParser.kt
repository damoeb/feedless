package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.RichFeed
import org.slf4j.LoggerFactory


class NullFeedParser : FeedParser {

  private val log = LoggerFactory.getLogger(NullFeedParser::class.simpleName)

  override fun canProcess(feedTypeAndContentType: Pair<FeedType, String>): Boolean {
    return true
  }

  override fun process(response: HarvestResponse): RichFeed {
    throw RuntimeException("No parser found for ${response.response.contentType}")
  }

}

