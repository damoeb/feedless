package org.migor.feedless.util

import org.jsoup.Jsoup
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.FeedType
import org.springframework.util.MimeType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FeedUtil {

  fun toURI(prefix: String, url: String, publishedAt: LocalDateTime? = null): String {
    // example tag:diveintomark.org,2004-05-27:1192 from https://web.archive.org/web/20080701231200/http://diveintomark.org/archives/2004/05/28/howto-atom-id
    val basic = "tag:feedless,${prefix},${
      URLEncoder.encode(
        url,
        StandardCharsets.UTF_8
      )
    }"
    return publishedAt?.let { basic + ":${publishedAt.format(DateTimeFormatter.ISO_DATE)}" } ?: basic
  }

  fun detectFeedTypeForResponse(response: HttpResponse): FeedType? {
    return detectFeedType(response.contentType) ?: guessFeedType(response)
  }

  fun isFeed(contentType: String): Boolean {
    return detectFeedType(contentType)?.let { true } ?: false
  }

  fun detectFeedType(contentType: String): FeedType? {
    val mimeType = MimeType.valueOf(contentType.lowercase())
    return when ("${mimeType.type}/${mimeType.subtype}") {
      "application/json" -> FeedType.JSON
      "text/rss+xml", "application/rss+xml",
      "application/atom+xml",
      "application/rdf+xml", "text/xml", "application/xml" -> FeedType.ATOM
      "text/calendar" -> FeedType.CALENDAR
      else -> null
    }
  }

//  private fun simpleContentType(harvestResponse: HttpResponse): String {
//    return harvestResponse.contentType.split(";")[0]
//  }

  private fun guessFeedType(harvestResponse: HttpResponse): FeedType? {
    val html = String(harvestResponse.responseBody)
    return if (html.trimStart().startsWith("<?xml ")) {
      kotlin.runCatching {
        Jsoup.parse(html)
        FeedType.ATOM
      }.getOrNull()
    } else {
      null
    }
  }

}
