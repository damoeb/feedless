package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.user.UserEntity
import java.util.*

data class Email(val subject: String, val text: String, val from: String)

interface MailService {

  suspend fun sendAuthCode(user: UserEntity, otp: OneTimePasswordEntity, description: String)

  suspend fun getNoReplyAddress(product: Vertical): String

  @Deprecated("")
  suspend fun send(mimeMessage: MimeMessage)

  suspend fun createMimeMessage(): MimeMessage

  suspend fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean)

}
