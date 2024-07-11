package org.migor.feedless.pipeline

import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.generated.types.PluginExecutionData

interface FragmentTransformerPlugin : FeedlessPlugin {

  fun transformFragment(
    corrId: String,
    action: ExecuteActionEntity,
    data: HttpResponse,
  ): PluginExecutionData

}
