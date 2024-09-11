package org.migor.feedless.pipeline

import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.scrape.LogCollector

interface FilterEntityPlugin : FeedlessPlugin {

  fun filterEntity(
    corrId: String,
    item: JsonItem,
    params: PluginExecutionParamsInput,
    index: Int,
    logCollector: LogCollector
  ): Boolean

}
