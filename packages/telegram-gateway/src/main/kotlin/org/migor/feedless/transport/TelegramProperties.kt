package org.migor.feedless.transport

import jakarta.validation.constraints.NotBlank
import org.migor.feedless.AppProfiles
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** The bot this instance talks to Telegram through; absent unless a token is configured. */
@Profile(AppProfiles.telegram)
@Validated
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('\${app.telegram.token:}')")
@ConfigurationProperties("app.telegram")
data class TelegramProperties(
  @field:NotBlank val token: String,
) {
  // the generated toString would put the token in every startup log
  override fun toString() = "TelegramProperties(token=***)"
}
