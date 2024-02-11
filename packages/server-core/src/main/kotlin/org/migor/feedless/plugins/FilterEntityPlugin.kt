package org.migor.feedless.plugins

import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput

interface FilterEntityPlugin : FeedlessPlugin {

  fun filterEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    params: PluginExecutionParamsInput
  ): Boolean

}
