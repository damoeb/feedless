package org.migor.feedless.mail

import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** The sender identity mails leave this instance with. */
@ConfigurationProperties("app.mail")
@Validated
@Profile(AppProfiles.properties)
data class MailGatewayProperties(
  val domain: String,
  val from: String,
)
