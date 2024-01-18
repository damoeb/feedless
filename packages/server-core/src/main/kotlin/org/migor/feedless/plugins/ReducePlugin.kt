package org.migor.feedless.plugins

import org.migor.feedless.data.jpa.models.WebDocumentEntity

interface ReducePlugin<T>: FeedlessPlugin {

  fun parseParams(params: String): T

  fun reduce(
    corrId: String,
    webDocuments: List<WebDocumentEntity>,
    params: T
  ): List<WebDocumentEntity>

}
