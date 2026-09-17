package org.migor.feedless.config

import jakarta.validation.constraints.NotBlank
import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** Who may call this instance from a browser, and who may read the actuator. */
@ConfigurationProperties("app")
@Validated
@Profile(AppProfiles.properties)
data class WebSecurityProperties(
  @field:NotBlank val actuatorPassword: String,
  val cors: CorsProperties,
) {
  // the generated toString would put the password in every startup log
  override fun toString() = "WebSecurityProperties(actuatorPassword=***, cors=$cors)"
}

data class CorsProperties(
  val allowedOrigins: List<String>,
)
