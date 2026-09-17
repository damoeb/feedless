package org.migor.feedless.status

import jakarta.validation.constraints.NotBlank
import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** What this build is: reported by /api/v1/status, the version header and the license check. */
@ConfigurationProperties("app.build")
@Validated
@Profile(AppProfiles.properties)
data class BuildInfo(
  @field:NotBlank val version: String,
  val commit: String,
  /** epoch millis as a string; empty outside a release image */
  val timestamp: String,
)
