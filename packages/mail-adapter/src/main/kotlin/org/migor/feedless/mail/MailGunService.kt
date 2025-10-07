package org.migor.feedless.mail

import com.mailgun.api.v3.MailgunMessagesApi
import org.migor.feedless.Vertical
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.user.User
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service

// see https://github.com/mailgun/mailgun-java
@Service
@ConditionalOnBean(MailgunMessagesApi::class)
class MailGunService : MailService {
  override suspend fun sendAuthCode(user: User, otp: OneTimePassword, description: String) {
    TODO("Not yet implemented")
  }

  override suspend fun getNoReplyAddress(product: Vertical): String {
    TODO("Not yet implemented")
  }

  override suspend fun send(email: Email) {
    TODO("Not yet implemented")
  }

}
