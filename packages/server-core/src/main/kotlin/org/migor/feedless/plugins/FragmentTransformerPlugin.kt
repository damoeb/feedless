package org.migor.feedless.plugins

import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.ScrapedElement

interface FragmentTransformerPlugin: FeedlessPluginWithDescription {

  fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    plugin: PluginExecution,
    url: String,
  ): Any

}
