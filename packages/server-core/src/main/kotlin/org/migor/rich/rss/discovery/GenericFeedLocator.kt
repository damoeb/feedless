package org.migor.rich.rss.discovery

import org.jsoup.nodes.Document
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.transform.GenericFeedRule
import org.migor.rich.rss.transform.WebToFeedTransformer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL

@Service
class GenericFeedLocator {
  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  fun locateInDocument(corrId: String, document: Document, url: String, strictMode: Boolean): List<GenericFeedRule> {
    return webToFeedTransformer.getArticleRules(corrId, document, URL(url), ArticleRecoveryType.NONE, strictMode, 15)
  }
}
