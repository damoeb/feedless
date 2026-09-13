package org.migor.feedless.pipeline

import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.pipeline.plugins.ItemFilterParams
import org.migor.feedless.scrape.LogCollector

interface ItemFilter {
  suspend fun filterEntity(item: JsonItem, params: List<ItemFilterParams>?, index: Int, logCollector: LogCollector): Boolean
}
