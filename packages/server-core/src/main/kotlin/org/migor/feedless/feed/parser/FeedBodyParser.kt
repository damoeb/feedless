package org.migor.feedless.feed.parser

import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.harvest.HarvestResponse

interface FeedBodyParser {
  fun priority(): Int
  fun canProcess(feedType: FeedType): Boolean
  fun process(corrId: String, response: HarvestResponse): RichFeed
}
