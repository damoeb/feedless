package org.migor.rss.rich.locate

import org.jsoup.nodes.Document

object DiscoveryLocator {

  /**
   * Locate a feed via RSS/Atom auto-discovery.  If both Atom and RSS are
   * listed we return both.
   */
  fun locate(document: Document): List<FeedReference> {

//    <link rel="alternate" type="application/rss+xml" title="yellowchicken &raquo; Feed" href="https://yellowchicken.wordpress.com/feed/" />

    val elements = document.select("link[rel=alternate][title][type]")
    return elements.map { element ->
      FeedReference(element.attr("href"), element.attr("type"), element.attr("title"))
    }
  }
}
