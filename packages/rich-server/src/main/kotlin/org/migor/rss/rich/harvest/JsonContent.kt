package org.migor.rss.rich.harvest

import com.google.gson.Gson
import org.slf4j.LoggerFactory

class JsonContent: ContentStrategy {

  private val log = LoggerFactory.getLogger(JsonContent::class.simpleName)

  private val gson = Gson()

  override fun canProcess(response: HarvestResponse): Boolean {
    TODO("")
//    val contentType = harvestResponse.contentType!!.split(";")[0]
//    return contentType.equals("application/json")
  }

  override fun process(response: HarvestResponse): RichFeed {
    TODO()
//    try {
//      // todo mag fix errors in Json
//      return RichFeed(gson.fromJson(harvestResponse.responseBody, SyndFeed::class.java), FeedType.JSON, harvestResponse.responses)
//    } catch (e: Exception) {
//      log.error("Unable to parse json", e)
//      log.debug(harvestResponse.responseBody)
//      throw e
//    }
  }

}
