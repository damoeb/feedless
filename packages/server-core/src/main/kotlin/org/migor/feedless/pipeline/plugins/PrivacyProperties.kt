package org.migor.feedless.pipeline.plugins

import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** Link targets the privacy plugin strips from a document. */
@ConfigurationProperties("app.privacy")
@Validated
@Profile(AppProfiles.properties)
data class PrivacyProperties(
  val blacklistedDomains: List<String>,
) {
  // APP_BLACKLISTED_DOMAINS was always space-separated, and comma-separated lists are the Boot convention
  fun domains(): Set<String> = blacklistedDomains.flatMap { it.trim().split(" ") }.filterNot { it.isBlank() }.toSet()
}
