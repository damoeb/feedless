package org.migor.rss.rich.harvest

import org.slf4j.LoggerFactory


class NullContent : ContentStrategy {

  private val log = LoggerFactory.getLogger(NullContent::class.simpleName)

  override fun canProcess(response: HarvestResponse): Boolean {
    log.info("Using null-content-strategy for ${response.response.contentType}")
    return true
  }

  override fun process(response: HarvestResponse): RichFeed {
    throw RuntimeException("Cannot process content ${response.url}, ${response.response.responseBody}")
  }

}

