package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.pipeline.plugins.MailData
import org.migor.feedless.user.UserEntity
import java.util.*

data class Email(val subject: String, val text: String, val from: String)

interface MailService {

  suspend fun sendWelcomeWaitListMail(user: UserEntity)

  suspend fun sendWelcomePaidMail(user: UserEntity)

  suspend fun sendWelcomeFreeMail(user: UserEntity)


  suspend fun sendAuthCode(user: UserEntity, otp: OneTimePasswordEntity, description: String)

  suspend fun getNoReplyAddress(product: ProductCategory): String

  @Deprecated("")
  suspend fun send(mimeMessage: MimeMessage)

  suspend fun createMimeMessage(): MimeMessage

  suspend fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean)

  suspend fun send(from: String, to: Array<String>, mailData: MailData)
}
