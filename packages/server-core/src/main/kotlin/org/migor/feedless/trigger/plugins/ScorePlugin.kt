package org.migor.feedless.trigger.plugins

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.models.FeatureState
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class ScorePlugin: WebDocumentPlugin {

  private val log = LoggerFactory.getLogger(ScorePlugin::class.simpleName)

  override fun id(): String = "score"

  override fun description(): String = "static quality score for feed items and web documents"

  override fun executionPhase(): PluginPhase = PluginPhase.finish

  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {

  }

  override fun enabled(): Boolean = true

  override fun configurableByUser(): Boolean = false

  override fun configurableInUserProfileOnly(): Boolean  = true

  override fun state(): FeatureState = FeatureState.experimental
}
