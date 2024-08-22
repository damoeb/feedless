package org.migor.feedless.pipeline

import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.PluginExecutionParamsInput

interface FilterEntityPlugin : FeedlessPlugin {

  fun filterEntity(
    corrId: String,
    item: JsonItem,
    params: PluginExecutionParamsInput,
    index: Int
  ): Boolean

}
