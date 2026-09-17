package org.migor.feedless.mail

import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

/** The Mailgun account mails are sent through, when one is configured. */
@ConfigurationProperties("app.mail")
@Validated
@Profile(AppProfiles.properties)
data class MailgunProperties(
  val mailgunKey: String,
) {
  // the generated toString would put the key in every startup log
  override fun toString() = "MailgunProperties(mailgunKey=***)"
}
