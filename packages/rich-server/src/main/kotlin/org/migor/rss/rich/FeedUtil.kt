package org.migor.rss.rich

import org.asynchttpclient.Response
import org.migor.rss.rich.feed.FeedType

object FeedUtil {
  fun detectFeedType(harvestResponse: Response): FeedType {
    return when(simpleContentType(harvestResponse)) {
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
