package org.migor.feedless.pipeline

import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput

interface FilterEntityPlugin : FeedlessPlugin {

  fun filterEntity(
      corrId: String,
      document: DocumentEntity,
      params: PluginExecutionParamsInput,
      index: Int
  ): Boolean

}
