package org.migor.feedless.pipeline

import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapedFeeds

data class FragmentOutput(
  val fragmentName: String,
  val items: List<JsonItem>? = null,
  val fragments: List<ScrapeExtractFragment>? = null,
  val feeds: ScrapedFeeds? = null
)
