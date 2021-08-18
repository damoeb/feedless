package org.migor.rss.rich.harvest.feedparser

import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestResponse
import org.slf4j.LoggerFactory


class NullFeedParser : FeedParser {

  private val log = LoggerFactory.getLogger(NullFeedParser::class.simpleName)

  override fun canProcess(feedType: FeedType): Boolean {
    return true
  }

  override fun process(response: HarvestResponse): FeedData {
    throw RuntimeException("No parser found for ${response.response.contentType}")
  }

}

