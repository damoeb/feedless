package org.migor.rss.rich.discovery

import org.jsoup.nodes.Document
import org.migor.rss.rich.transform.GenericFeedRule
import org.migor.rss.rich.transform.WebToFeedTransformer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL

@Service
class GenericFeedLocator {
  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  fun locateInDocument(corrId: String, document: Document, url: String): List<GenericFeedRule> {
    return webToFeedTransformer.getArticleRules(corrId, document, URL(url), 3)
  }
}
