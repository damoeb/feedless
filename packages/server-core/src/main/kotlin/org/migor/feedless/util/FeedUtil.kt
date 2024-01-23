package org.migor.feedless.util

import org.jsoup.Jsoup
import org.migor.feedless.feed.parser.FeedType
import org.migor.feedless.service.HttpResponse
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object FeedUtil {

  private val uriDateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

  fun toURI(prefix: String, url: String, publishedAt: Date? = null): String {
    // example tag:diveintomark.org,2004-05-27:1192 from https://web.archive.org/web/20080701231200/http://diveintomark.org/archives/2004/05/28/howto-atom-id
    val basic = "tag:feedless,${prefix},${
      URLEncoder.encode(
        url,
        StandardCharsets.UTF_8
      )
    }"
    return Optional.ofNullable(publishedAt).map { basic + ":${uriDateFormatter.format(publishedAt)}" }.orElse(basic)
  }

  fun detectFeedTypeForResponse(response: HttpResponse): Pair<FeedType, String> {
    val mimeType = response.contentType
    val contentType = simpleContentType(response)
    return try {
      Pair(detectFeedType(contentType), mimeType)
    } catch (e: RuntimeException) {
      Pair(guessFeedType(response), mimeType)
    }
  }

  fun detectFeedType(contentType: String): FeedType {
    return when (contentType) {
      "application/json" -> FeedType.JSON
      "application/rss+xml" -> FeedType.RSS
      "application/atom+xml" -> FeedType.ATOM
      "text/xml", "application/xml" -> FeedType.XML
      else -> throw RuntimeException("Cannot resolve feedType $contentType")
    }
  }

  private fun simpleContentType(harvestResponse: HttpResponse): String {
    return harvestResponse.contentType.split(";")[0]
  }

  private fun guessFeedType(harvestResponse: HttpResponse): FeedType {
    val html = String(harvestResponse.responseBody)
    return if (html.trimStart().startsWith("<?xml ")) {
      kotlin.runCatching {
        Jsoup.parse(html)
        FeedType.NONE
      }.getOrElse { FeedType.XML }
    } else {
      FeedType.NONE
    }
  }

}
