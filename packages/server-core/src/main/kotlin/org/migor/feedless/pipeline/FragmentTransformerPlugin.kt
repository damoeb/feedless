package org.migor.feedless.pipeline

import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.ScrapedElement

interface FragmentTransformerPlugin : FeedlessPlugin {

  fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    plugin: PluginExecution,
    url: String,
  ): Any

}
