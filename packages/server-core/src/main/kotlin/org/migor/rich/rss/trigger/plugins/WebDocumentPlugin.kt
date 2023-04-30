package org.migor.rich.rss.trigger.plugins

import org.migor.rich.rss.data.jpa.models.WebDocumentEntity

interface WebDocumentPlugin {
  fun id(): String
  fun executionPriority(): Int

  fun processWebDocument(corrId: String, webDocument: WebDocumentEntity)
}
