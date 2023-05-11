package org.migor.feedless.trigger.plugins

import org.migor.feedless.data.jpa.models.FeatureState
import org.migor.feedless.data.jpa.models.WebDocumentEntity

interface WebDocumentPlugin {
  fun id(): String
  fun description(): String
  fun executionPhase(): PluginPhase
  fun processWebDocument(corrId: String, webDocument: WebDocumentEntity)
  fun configurableInUserProfileOnly(): Boolean
  fun enabled(): Boolean
  fun configurableByUser(): Boolean
  fun state(): FeatureState
}
