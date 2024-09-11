package org.migor.feedless.pipeline

import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapedFeeds
import org.migor.feedless.scrape.LogCollector

data class FragmentOutput(
  val fragmentName: String,
  val items: List<JsonItem>? = null,
  val fragments: List<ScrapeExtractFragment>? = null,
  val feeds: ScrapedFeeds? = null
)

interface FragmentTransformerPlugin : FeedlessPlugin {

  suspend fun transformFragment(
      corrId: String,
      action: ExecuteActionEntity,
      data: HttpResponse,
      logger: LogCollector,
  ): FragmentOutput

}
