package org.migor.rss.rich.discovery

import org.jsoup.nodes.Document

object LinkLocator {
  fun locate(document: Document): List<FeedReference> {
    val elements = document.select("a[rel=alternate][href][type]")
    return elements.map { element ->
      FeedReference(element.attr("href"), element.attr("type"), element.attr("title"))
    }
  }

}
