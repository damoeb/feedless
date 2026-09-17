package org.migor.feedless.analytics

import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** The Plausible instance this deployment reports page views to. */
@ConfigurationProperties("app.analytics")
@Validated
@Profile(AppProfiles.properties)
data class AnalyticsProperties(
  val plausibleUrl: String = "",
  val plausibleSite: String = "",
  val plausibleApiKey: String = "",
) {
  // the generated toString would put the api key in every startup log
  override fun toString() = "AnalyticsProperties(plausibleUrl=$plausibleUrl, plausibleSite=$plausibleSite, plausibleApiKey=***)"
}
