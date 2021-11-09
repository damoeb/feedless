package org.migor.rss.rich.discovery

import org.jsoup.nodes.Document
import org.migor.rss.rich.parser.GenericFeedRule
import org.migor.rss.rich.parser.MarkupToFeedParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL

@Service
class GenericFeedLocator {
  @Autowired
  lateinit var markupToFeedParser: MarkupToFeedParser

  fun locateInDocument(document: Document, url: String): List<GenericFeedRule> {
    return markupToFeedParser.getArticleRules(document, URL(url), 2)
  }
}
