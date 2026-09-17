package org.migor.feedless.common

import jakarta.validation.constraints.NotBlank
import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** Where this instance is reachable: the API gateway and the web app. */
@ConfigurationProperties("app")
@Validated
@Profile(AppProfiles.properties)
data class PublicUrls(
  @field:NotBlank val apiGatewayUrl: String,
  @field:NotBlank val appHost: String,
) {
  init {
    require(!apiGatewayUrl.startsWith("\${")) { "app.apiGatewayUrl is unresolved; set APP_API_GATEWAY_URL" }
    require(!appHost.startsWith("\${")) { "app.appHost is unresolved; set APP_HOST_URL" }
  }
}
