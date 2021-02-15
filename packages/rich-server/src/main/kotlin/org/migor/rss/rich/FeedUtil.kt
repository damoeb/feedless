package org.migor.rss.rich

import org.asynchttpclient.Response
import org.migor.rss.rich.feed.FeedType
import java.text.SimpleDateFormat
import java.util.*

object FeedUtil {

  private val uriDateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

  fun toURI(entryId: String, subscriptionId: String, createdAt: Date): String {
    // example tag:diveintomark.org,2004-05-27:1192 from https://web.archive.org/web/20080701231200/http://diveintomark.org/archives/2004/05/28/howto-atom-id
    return "tag:rich-rss.migor.org,${uriDateFormatter.format(createdAt)}:subscription:${subscriptionId}/entry:${entryId}"
  }

  fun detectFeedType(harvestResponse: Response): FeedType {
    return when (simpleContentType(harvestResponse)) {
//      "application/json" -> FeedType.JSON
      "application/rss+xml" -> FeedType.RSS
      "application/atom+xml" -> FeedType.ATOM
      else -> guessFeedType(harvestResponse)
    }
  }

  private fun simpleContentType(harvestResponse: Response): String {
    return harvestResponse.contentType!!.split(";")[0]
  }

  private fun guessFeedType(harvestResponse: Response): FeedType {
    if (harvestResponse.responseBody.trimStart().startsWith("<?xml ")) {
      return FeedType.XML
    }
    return FeedType.NONE
  }
}
