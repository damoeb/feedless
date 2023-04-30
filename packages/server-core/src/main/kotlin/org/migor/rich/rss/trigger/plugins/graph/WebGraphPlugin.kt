package org.migor.rich.rss.trigger.plugins.graph

import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.trigger.plugins.WebDocumentPlugin
import org.springframework.data.domain.PageRequest

abstract class WebGraphPlugin: WebDocumentPlugin {

  override fun id(): String = "webGraph"

  override fun executionPriority(): Int = 20

  abstract fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity>
}
