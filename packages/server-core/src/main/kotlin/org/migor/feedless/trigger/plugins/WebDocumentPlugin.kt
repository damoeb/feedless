package org.migor.feedless.trigger.plugins

import org.migor.feedless.data.jpa.models.WebDocumentEntity

interface WebDocumentPlugin {
  fun id(): String
  fun executionPriority(): Int

  fun processWebDocument(corrId: String, webDocument: WebDocumentEntity)
}
