package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.pipeline.plugins.MailData
import org.migor.feedless.secrets.OneTimePasswordEntity
import org.migor.feedless.user.UserEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.database} & !${AppProfiles.mail}")
class NoopMailService : MailService {
  override fun sendWelcomeWaitListMail(corrId: String, user: UserEntity) {
  }

  override fun sendWelcomePaidMail(corrId: String, user: UserEntity) {
  }

  override fun sendWelcomeFreeMail(corrId: String, user: UserEntity) {
  }

  override fun sendAuthCode(corrId: String, user: UserEntity, otp: OneTimePasswordEntity, description: String) {
  }

  override fun getNoReplyAddress(product: ProductCategory): String {
    return "no-reply@example"
  }

  @Deprecated("")
  override fun send(corrId: String, mimeMessage: MimeMessage) {
  }

  override fun createMimeMessage(): MimeMessage {
    throw IllegalArgumentException("not implemented")
  }

  override fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean) {
  }

  override fun send(corrId: String, from: String, to: Array<String>, mailData: MailData) {
  }

}
