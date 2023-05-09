package org.migor.feedless.trigger.plugins.graph

import org.migor.feedless.data.jpa.models.ArticleEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.trigger.plugins.WebDocumentPlugin
import org.springframework.data.domain.PageRequest

abstract class WebGraphPlugin: WebDocumentPlugin {

  override fun id(): String = "webGraph"

  override fun executionPriority(): Int = 20

  abstract fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity>
}
