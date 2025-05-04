package org.migor.feedless.transport

import org.migor.feedless.AppProfiles
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Profile(AppProfiles.telegram)
@Component
@Validated
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('\${app.telegram.token:}')")
@ConfigurationProperties(prefix = "app.telegram")
class TelegramProperties {
  lateinit var token: String
}
