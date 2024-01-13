package org.migor.feedless.plugins

import org.migor.feedless.data.jpa.models.WebDocumentEntity

interface EntityTransformerPlugin: FeedlessPlugin {

  fun transformEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    paramsRaw: String?
  )

}
