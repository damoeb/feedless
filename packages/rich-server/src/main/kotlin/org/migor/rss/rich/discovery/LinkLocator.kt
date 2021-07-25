package org.migor.rss.rich.discovery

import org.jsoup.nodes.Document
import org.migor.rss.rich.util.FeedUtil

object LinkLocator {
  fun locate(document: Document): List<FeedReference> {
    val elements = document.select("a[rel=alternate][href][type]")
    return elements.map { element ->
      FeedReference(
        element.attr("href"),
        FeedUtil.detectFeedType(element.attr("type")).first,
        element.attr("title"))
    }
  }

}
