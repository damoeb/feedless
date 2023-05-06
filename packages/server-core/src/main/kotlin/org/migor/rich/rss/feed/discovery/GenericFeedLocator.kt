package org.migor.rich.rss.feed.discovery

import org.jsoup.nodes.Document
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.web.GenericFeedParserOptions
import org.migor.rich.rss.web.GenericFeedRule
import org.migor.rich.rss.web.WebToFeedTransformer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL

@Service
class GenericFeedLocator {
  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  fun locateInDocument(
    corrId: String,
    document: Document,
    url: String,
    parserOptions: GenericFeedParserOptions
  ): List<GenericFeedRule> {
    return webToFeedTransformer.parseFeedRules(corrId, document, URL(url), ArticleRecoveryType.NONE, parserOptions, 15)
  }
}
