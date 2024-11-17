package org.migor.feedless.pipeline

import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.scrape.LogCollector

interface FilterEntityPlugin : FeedlessPlugin {

  suspend fun filterEntity(
    item: JsonItem,
    params: PluginExecutionJsonEntity,
    index: Int,
    logCollector: LogCollector
  ): Boolean

}
