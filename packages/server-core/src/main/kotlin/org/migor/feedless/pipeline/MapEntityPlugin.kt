package org.migor.feedless.pipeline

import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput

interface MapEntityPlugin : FeedlessPlugin {

  fun mapEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  )

}
