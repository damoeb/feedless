package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.pipeline.plugins.MailData
import org.migor.feedless.user.UserEntity
import java.util.*

data class Email(val subject: String, val text: String, val from: String)

interface MailService {

  fun sendWelcomeWaitListMail(corrId: String, user: UserEntity)

  fun sendWelcomePaidMail(corrId: String, user: UserEntity)

  fun sendWelcomeFreeMail(corrId: String, user: UserEntity)


  fun sendAuthCode(corrId: String, user: UserEntity, otp: OneTimePasswordEntity, description: String)

  fun getNoReplyAddress(product: ProductCategory): String

  @Deprecated("")
  fun send(corrId: String, mimeMessage: MimeMessage)

  fun createMimeMessage(): MimeMessage

  suspend fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean)

  fun send(corrId: String, from: String, to: Array<String>, mailData: MailData)
}
