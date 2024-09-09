package org.migor.feedless.feed.discovery

import org.jsoup.nodes.Document
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.WebToFeedTransformer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URI

@Service
class GenericFeedLocator {
  @Autowired
  private lateinit var webToFeedTransformer: WebToFeedTransformer

  suspend fun locateInDocument(
    corrId: String,
    document: Document,
    url: String,
    parserOptions: GenericFeedParserOptions
  ): List<GenericFeedRule> {
    return webToFeedTransformer.parseFeedRules(corrId, document, URI(url), parserOptions)
  }
}
