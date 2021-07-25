package org.migor.rss.rich.discovery

import org.asynchttpclient.Response
import org.jsoup.Jsoup
import org.migor.rss.rich.harvest.feedparser.FeedType
import org.migor.rss.rich.util.FeedUtil


object NativeFeedLocator {

  fun locate(response: Response, url: String): List<FeedReference> {
    val pair = FeedUtil.detectFeedTypeForResponse(response)
    return if (pair.first == FeedType.NONE) {
      val document = Jsoup.parse(response.responseBody)
      //    <link rel="alternate" type="application/rss+xml" title="yellowchicken &raquo; Feed" href="https://yellowchicken.wordpress.com/feed/" />

      document.select("link[rel=alternate][title][type]")
        .map { element ->
          FeedReference(
            element.attr("href"),
            FeedUtil.detectFeedType(element.attr("type")).first,
            element.attr("title"))
        }
    } else {
      listOf(FeedReference(url = url, type = FeedUtil.detectFeedTypeForResponse(response).first, title = "Feed"))
    }
  }
}
