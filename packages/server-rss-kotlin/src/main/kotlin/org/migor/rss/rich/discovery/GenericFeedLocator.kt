package org.migor.rss.rich.discovery

import org.jsoup.nodes.Document
import org.migor.rss.rich.parser.GenericFeedRule
import org.migor.rss.rich.parser.WebToFeedParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL

@Service
class GenericFeedLocator {
  @Autowired
  lateinit var webToFeedParser: WebToFeedParser

  fun locateInDocument(document: Document, url: String): List<GenericFeedRule> {
    return webToFeedParser.getArticleRules(document, URL(url), 3)
  }
}
