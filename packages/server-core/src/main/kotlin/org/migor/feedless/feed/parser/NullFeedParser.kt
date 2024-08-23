package org.migor.feedless.feed.parser

import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.json.JsonFeed

class NullFeedParser : FeedBodyParser {

  override fun priority(): Int {
    return 0
  }

  override fun canProcess(feedType: FeedType): Boolean {
    return true
  }

  override fun process(corrId: String, response: HttpResponse): JsonFeed {
    throw IllegalArgumentException("No feed parser found for ${response.contentType} ($corrId)")
  }
}
