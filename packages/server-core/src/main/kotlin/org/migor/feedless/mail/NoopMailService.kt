package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.pipeline.plugins.MailData
import org.migor.feedless.user.UserEntity
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
@ConditionalOnBean(MailService::class)
class NoopMailService : MailService {
  override suspend fun sendWelcomeWaitListMail(user: UserEntity) {
  }

  override suspend fun sendWelcomePaidMail(user: UserEntity) {
  }

  override suspend fun sendWelcomeFreeMail(user: UserEntity) {
  }

  override suspend fun sendAuthCode(user: UserEntity, otp: OneTimePasswordEntity, description: String) {
  }

  override suspend fun getNoReplyAddress(product: ProductCategory): String {
    return "no-reply@example"
  }

  @Deprecated("")
  override suspend fun send(mimeMessage: MimeMessage) {
  }

  override suspend fun createMimeMessage(): MimeMessage {
    throw IllegalArgumentException("not implemented")
  }

  override suspend fun updateMailForwardById(mailForwardId: UUID, authorize: Boolean) {
  }

  override suspend fun send(from: String, to: Array<String>, mailData: MailData) {
  }

}
