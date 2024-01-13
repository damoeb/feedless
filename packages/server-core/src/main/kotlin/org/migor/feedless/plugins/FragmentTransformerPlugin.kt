package org.migor.feedless.plugins

import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.Transformer

interface FragmentTransformerPlugin: FeedlessPlugin {

  fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    transformer: Transformer,
    url: String,
  ): Any

}
