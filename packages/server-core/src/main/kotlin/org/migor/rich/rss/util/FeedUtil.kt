package org.migor.rich.rss.util

import org.asynchttpclient.Response
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.feedparser.FeedType
import org.springframework.util.MimeType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object FeedUtil {

  private val uriDateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

  // see https://stackoverflow.com/questions/15247742/rfc-822-date-time-format-in-rss-2-0-feeds-cet-not-accepted
  private val rfc822DateFormatter: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
  private val rfc3339DateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")

  fun toURI(prefix: String, createdAt: Date, url: String): String {
    // example tag:diveintomark.org,2004-05-27:1192 from https://web.archive.org/web/20080701231200/http://diveintomark.org/archives/2004/05/28/howto-atom-id
    return "tag:rich-rss,${prefix},${uriDateFormatter.format(createdAt)}:${
      URLEncoder.encode(
        url,
        StandardCharsets.UTF_8
      )
    }"
  }

  fun formatAsRFC822(date: Date): String {
    return rfc822DateFormatter.format(date)
  }

  fun formatAsRFC3339(date: Date): String {
    return rfc3339DateFormatter.format(date)
  }

  fun cleanMetatags(value: String): String {
    return value.removePrefix("<![CDATA[").removeSuffix("]]")
  }

  fun detectFeedTypeForResponse(response: Response): Pair<FeedType, MimeType?> {

    val mimeType = MimeType.valueOf(response.contentType)
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

  fun resolveArticleRecovery(articleResolution: String?): ArticleRecovery {
    val fallback = ArticleRecovery.NONE
    return runCatching {
      Optional.ofNullable(articleResolution).map { ArticleRecovery.valueOf(it) }.orElse(fallback)
    }.getOrElse { fallback }
  }

  fun simpleContentType(harvestResponse: Response): String {
    return harvestResponse.contentType!!.split(";")[0]
  }

  fun guessFeedType(harvestResponse: Response): FeedType {
    if (harvestResponse.responseBody.trimStart().startsWith("<?xml ")) {
      return FeedType.XML
    }
    return FeedType.NONE
  }
}
