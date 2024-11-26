package org.migor.feedless.mail

import com.mailgun.api.v3.MailgunMessagesApi
import jakarta.mail.internet.MimeMessage
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.user.UserEntity
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service

// see https://github.com/mailgun/mailgun-java
@Service
@ConditionalOnBean(MailgunMessagesApi::class)
class MailGunService: MailService {
  override suspend fun sendAuthCode(user: UserEntity, otp: OneTimePasswordEntity, description: String) {
    TODO("Not yet implemented")
  }

  override suspend fun getNoReplyAddress(product: Vertical): String {
    TODO("Not yet implemented")
  }

  override suspend fun send(mimeMessage: MimeMessage) {
    TODO("Not yet implemented")
  }

  override suspend fun createMimeMessage(): MimeMessage {
    TODO("Not yet implemented")
  }

}
