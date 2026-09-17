package org.migor.feedless.license

import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** The license this instance runs under, and the key it is verified with. */
@ConfigurationProperties("app.license")
@Validated
@Profile(AppProfiles.properties)
data class LicenseProperties(
  val key: String,
  val pemFile: String,
) {
  // the generated toString would put the license key in every startup log
  override fun toString() = "LicenseProperties(key=***, pemFile=$pemFile)"
}
