package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.RichFeed
import org.slf4j.LoggerFactory


class NullFeedParser : FeedParser {

  private val log = LoggerFactory.getLogger(NullFeedParser::class.simpleName)

  override fun canProcess(response: HarvestResponse): Boolean {
    log.info("Using null-content-strategy for ${response.response.contentType}")
    return true
  }

  override fun process(response: HarvestResponse): RichFeed {
    throw RuntimeException("Cannot process content ${response.url}, ${response.response.responseBody}")
  }

}

