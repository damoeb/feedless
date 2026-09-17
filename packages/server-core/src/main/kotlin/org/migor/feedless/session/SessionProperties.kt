package org.migor.feedless.session

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated
import java.time.Duration
import java.time.temporal.ChronoUnit

/** How this instance signs session tokens and which hosts may authenticate without one. */
@ConfigurationProperties("app")
@Validated
@Profile(AppProfiles.properties)
data class SessionProperties(
  @field:NotBlank @field:Size(min = jwtSecretMinLength) val jwtSecret: String,
  val whitelistedHosts: List<String> = emptyList(),
  val auth: AnonymousTokenProperties = AnonymousTokenProperties(),
) {
  init {
    require(!jwtSecret.startsWith("\${")) { "app.jwtSecret is unresolved; set APP_JWT_SECRET" }
  }

  // the generated toString would put the secret in every startup log
  override fun toString() = "SessionProperties(jwtSecret=***, whitelistedHosts=$whitelistedHosts, auth=$auth)"

  companion object {
    const val jwtSecretMinLength: Int = 10
  }
}

data class AnonymousTokenProperties(
  @param:DurationUnit(ChronoUnit.DAYS) val anonymousTokenValidFor: Duration = Duration.ofDays(28),
)
