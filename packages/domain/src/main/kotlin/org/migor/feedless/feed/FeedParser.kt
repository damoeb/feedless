package org.migor.feedless.feed

import org.migor.feedless.feed.parser.json.JsonFeed

interface FeedParser {
  suspend fun parseFeedFromUrl(url: String): JsonFeed
}
