package org.migor.rss.rich.harvest

import com.google.gson.Gson
import com.rometools.rome.feed.synd.SyndFeed
import org.slf4j.LoggerFactory

class JsonContent: ContentStrategy {

  private val log = LoggerFactory.getLogger(JsonContent::class.simpleName)

  private val gson = Gson()

  override fun canProcess(harvestResponse: HarvestResponse): Boolean {
    val contentType = harvestResponse.contentType!!.split(";")[0]
    return contentType.equals("application/json")
  }

  override fun process(harvestResponse: HarvestResponse): RichFeed {
    try {
      // todo mag fix errors in Json
      return RichFeed(gson.fromJson(harvestResponse.responseBody, SyndFeed::class.java), FeedType.JSON)
    } catch (e: Exception) {
      log.error("Unable to parse json", e)
      log.debug(harvestResponse.responseBody)
      throw e
    }
  }

}
