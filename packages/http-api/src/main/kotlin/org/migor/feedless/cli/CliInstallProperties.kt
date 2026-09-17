package org.migor.feedless.cli

import jakarta.validation.constraints.NotBlank
import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** Where the feedctl install script is served from. */
@ConfigurationProperties("app.cli")
@Validated
@Profile(AppProfiles.properties)
data class CliInstallProperties(
  @field:NotBlank val installScriptLocation: String,
)
