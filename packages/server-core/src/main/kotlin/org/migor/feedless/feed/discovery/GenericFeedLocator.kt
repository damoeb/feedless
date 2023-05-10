package org.migor.feedless.feed.discovery

import org.jsoup.nodes.Document
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.WebToFeedTransformer
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
    return webToFeedTransformer.parseFeedRules(corrId, document, URL(url), parserOptions, 15)
  }
}
