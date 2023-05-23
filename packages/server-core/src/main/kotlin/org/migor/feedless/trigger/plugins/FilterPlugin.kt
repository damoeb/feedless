package org.migor.feedless.trigger.plugins

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.FeatureState
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class FilterPlugin: WebDocumentPlugin {

  private val log = LoggerFactory.getLogger(FilterPlugin::class.simpleName)

  override fun id(): String = "filter"
  override fun description(): String = "Conditions to add or remove documents"
  override fun executionPhase(): PluginPhase = PluginPhase.purate
  override fun state(): FeatureState = FeatureState.experimental
  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {

  }

  override fun enabled(): Boolean = true
  override fun configurableByUser(): Boolean = true
  override fun configurableInUserProfileOnly(): Boolean  = true
}
