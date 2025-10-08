package org.migor.feedless.mail

import com.mailgun.api.v3.MailgunMessagesApi
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.user.User
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Profile

// see https://github.com/mailgun/mailgun-java
//@Service
@Profile("${AppProfiles.saas} & ${AppProfiles.mail} & ${AppLayer.service}")
@ConditionalOnBean(MailgunMessagesApi::class)
class MailGunService : MailService {
  override suspend fun sendAuthCode(user: User, otp: OneTimePassword, description: String) {
    TODO("Not yet implemented")
  }

  override suspend fun send(email: Email) {
    TODO("Not yet implemented")
  }

}
