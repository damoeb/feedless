package org.migor.rss.rich.util

import org.asynchttpclient.Response
import org.migor.rss.rich.harvest.feedparser.FeedType
import java.text.SimpleDateFormat
import java.util.*

object FeedUtil {

  private val uriDateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

  // see https://stackoverflow.com/questions/15247742/rfc-822-date-time-format-in-rss-2-0-feeds-cet-not-accepted
  private val rfc822DateFormatter: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
  private val rfc3339DateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")

  fun toURI(entryId: String, subscriptionId: String, createdAt: Date): String {
    // example tag:diveintomark.org,2004-05-27:1192 from https://web.archive.org/web/20080701231200/http://diveintomark.org/archives/2004/05/28/howto-atom-id
    return "tag:rich-rss.migor.org,${uriDateFormatter.format(createdAt)}:subscription:${subscriptionId}/entry:${entryId}"
  }

  fun formatAsRFC822(date: Date): String {
    return rfc822DateFormatter.format(date)
  }

  fun formatAsRFC3339(date: Date): String {
    return rfc3339DateFormatter.format(date)
  }

  fun detectFeedTypeForResponse(response: Response): Pair<FeedType, String> {

    val contentType = simpleContentType(response)
    return try {
      detectFeedType(contentType);
    } catch (e: RuntimeException) {
      Pair(guessFeedType(response), contentType)
    }
  }
  fun detectFeedType(contentType: String): Pair<FeedType, String> {
    return when (contentType) {
      "application/json" -> Pair(FeedType.JSON, contentType)
      "application/rss+xml" -> Pair(FeedType.RSS, contentType)
      "application/atom+xml" -> Pair(FeedType.ATOM, contentType)
      "text/xml", "application/xml" -> Pair(FeedType.XML, contentType)
      else -> throw RuntimeException("Cannot resolve feedType ${contentType}")
    }
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
