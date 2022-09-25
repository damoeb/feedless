package org.migor.rich.rss.exporter

import com.google.gson.GsonBuilder
import org.migor.rich.rss.api.dto.RichFeed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class JsonFeedExporter {
  private val FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ss-Z"

  private val log = LoggerFactory.getLogger(JsonFeedExporter::class.simpleName)

  //  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder()
    .setDateFormat(FORMAT_RFC3339) // https://tools.ietf.org/html/rfc3339
    .create()

  private fun toJsonFeedUrlForPage(feed: RichFeed, page: Int? = null): String {
    return toFeedUrlForPage(feed, "json", page)
  }

  private fun toFeedUrlForPage(feed: RichFeed, type: String, page: Int? = null): String {
    return Optional.ofNullable(page).map { actualPage -> "${feed.feed_url}/${type}?page=${actualPage}" }
      .orElse(feed.feed_url)
  }

  fun toJson(corrId: String, feed: RichFeed): String {
    log.info("[${corrId}] to json")

    feed.selfPage?.let {
      if (feed.lastPage != feed.selfPage) {
        feed.next_url = toJsonFeedUrlForPage(feed, feed.selfPage + 1)
      }
      if (feed.selfPage != 0) {
        feed.previous_url = toJsonFeedUrlForPage(feed, feed.selfPage - 1)
      }
    }
    feed.last_url = toJsonFeedUrlForPage(feed, feed.lastPage)
    return gson.toJson(feed)
  }

}
