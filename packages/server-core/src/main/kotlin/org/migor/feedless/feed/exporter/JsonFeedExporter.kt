package org.migor.feedless.feed.exporter

import com.google.gson.GsonBuilder
import org.migor.feedless.feed.parser.json.JsonFeed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JsonFeedExporter {
  private val FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ss-Z"

  private val log = LoggerFactory.getLogger(JsonFeedExporter::class.simpleName)

  //  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder()
    .setDateFormat(FORMAT_RFC3339) // https://tools.ietf.org/html/rfc3339
    .create()

  fun toJson(corrId: String, feed: JsonFeed): String {
    log.debug("[${corrId}] to json")

//    feed.selfPage?.let {
//      if (feed.items.isNotEmpty()) {
//        feed.nextUrl = toJsonFeedUrlForPage(feed, it + 1)
//      }
////      if (feed.selfPage != 0) {
////        feed.previousUrl = toJsonFeedUrlForPage(feed, it - 1)
////      }
//    }
//    feed.lastUrl = toJsonFeedUrlForPage(feed, feed.lastPage)
    return gson.toJson(feed)
  }

}
