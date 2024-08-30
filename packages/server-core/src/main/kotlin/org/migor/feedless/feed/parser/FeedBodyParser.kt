package org.migor.feedless.feed.parser

import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.json.JsonFeed

interface FeedBodyParser {
  fun priority(): Int
  fun canProcess(feedType: FeedType): Boolean
  suspend fun process(corrId: String, response: HttpResponse): JsonFeed
}
