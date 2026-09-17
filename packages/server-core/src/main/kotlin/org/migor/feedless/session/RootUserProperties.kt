package org.migor.feedless.session

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** The root account this instance seeds and authenticates. */
@ConfigurationProperties("app")
@Validated
@Profile(AppProfiles.properties)
data class RootUserProperties(
  @field:NotBlank val rootEmail: String,
  @field:NotBlank @field:Size(min = rootSecretKeyMinLength) val rootSecretKey: String,
) {
  init {
    require(!rootEmail.startsWith("\${")) { "app.rootEmail is unresolved; set APP_ROOT_EMAIL" }
    require(!rootSecretKey.startsWith("\${")) { "app.rootSecretKey is unresolved; set APP_ROOT_SECRET_KEY" }
  }

  // the generated toString would put the secret in every startup log
  override fun toString() = "RootUserProperties(rootEmail=$rootEmail, rootSecretKey=***)"

  companion object {
    const val rootSecretKeyMinLength: Int = 7
  }
}
