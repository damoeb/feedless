package org.migor.feedless.scrape

import org.migor.feedless.feed.parser.json.JsonFeed

interface WebToFeed {
  /** Takes raw HTML so the caller needs no parsed document; the feed title comes from the page. */
  suspend fun webToFeed(html: String, url: String, selectors: GenericFeedSelectors, logger: LogCollector): JsonFeed
}
