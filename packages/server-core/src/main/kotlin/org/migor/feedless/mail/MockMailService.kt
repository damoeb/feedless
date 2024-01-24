package org.migor.feedless.mail

import jakarta.mail.internet.MimeMessage
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.Product
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!${AppProfiles.mail}")
class MockMailService: MailService {
  private val log = LoggerFactory.getLogger(MockMailService::class.simpleName)

  override fun sendWelcomeMail(corrId: String, user: UserEntity) {
    log.debug("[$corrId] send welcome mail ${user.email}")
  }

  override fun sendAuthCode(corrId: String, user: UserEntity, otp: OneTimePasswordEntity, description: String) {
    log.debug("[$corrId] send auth-code mail ${otp.password}")
  }

  override fun getNoReplyAddress(product: Product) = "mock@localhost"
}
