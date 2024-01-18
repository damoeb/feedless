package org.migor.feedless.plugins

import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.PluginExecution

interface FragmentTransformerPlugin: FeedlessPluginWithDescription {

  fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    plugin: PluginExecution,
    url: String,
  ): Any

}
