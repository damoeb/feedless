package org.migor.feedless.trigger.plugins.graph

import org.migor.feedless.data.jpa.models.ArticleEntity
import org.migor.feedless.data.jpa.models.FeatureState
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.trigger.plugins.PluginPhase
import org.migor.feedless.trigger.plugins.WebDocumentPlugin
import org.springframework.data.domain.PageRequest

abstract class WebGraphPlugin: WebDocumentPlugin {

  override fun id(): String = "webGraph"

  override fun description(): String = "Inspect the "

  override fun executionPhase(): PluginPhase = PluginPhase.harvest

  override fun configurableByUser(): Boolean = false

  override fun state(): FeatureState = FeatureState.stable

  abstract fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity>
}
