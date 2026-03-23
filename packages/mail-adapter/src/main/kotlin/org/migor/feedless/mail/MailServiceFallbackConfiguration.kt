package org.migor.feedless.mail

import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.user.User
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Provides a no-op [MailService] when the real implementation is not active
 * (e.g. [AppProfiles.mail] is off). Production with the mail profile still
 * registers [MailServiceImpl] and wins via [ConditionalOnMissingBean].
 */
@Configuration
class MailServiceFallbackConfiguration {

  @Bean
  @ConditionalOnMissingBean(MailService::class)
  fun noopMailService(): MailService = object : MailService {
    override suspend fun sendAuthCode(user: User, otp: OneTimePassword, description: String) {}

    override suspend fun send(outgoingMail: OutgoingMail) {}
  }
}
