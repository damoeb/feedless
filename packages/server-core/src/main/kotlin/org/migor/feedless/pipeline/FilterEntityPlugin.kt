package org.migor.feedless.pipeline

import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.scrape.LogCollector

interface FilterEntityPlugin<T> : FeedlessPlugin {

  suspend fun filterEntity(
    item: JsonItem,
    params: T,
    index: Int,
    logCollector: LogCollector
  ): Boolean

  suspend fun filterEntity(
    item: JsonItem,
    jsonParams: String?,
    index: Int,
    logCollector: LogCollector
  ): Boolean

  suspend fun fromJson(
    jsonParams: String?,
  ): T

}
