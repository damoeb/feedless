package org.migor.feedless.feed.discovery

import org.jsoup.nodes.Document
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.scrape.GenericFeedParserOptions
import org.migor.feedless.scrape.GenericFeedRule
import org.migor.feedless.scrape.WebToFeedTransformer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.URI

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class GenericFeedLocator(
  private val webToFeedTransformer: WebToFeedTransformer
) {

  suspend fun locateInDocument(
    document: Document,
    url: String,
    parserOptions: GenericFeedParserOptions
  ): List<GenericFeedRule> {
    return webToFeedTransformer.parseFeedRules(document, URI(url), parserOptions)
  }
}
