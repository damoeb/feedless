package org.migor.feedless.mail.gateway

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import org.migor.feedless.AppProfiles
import org.migor.feedless.mail.MailGateway
import org.migor.feedless.mail.MailGatewayProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender

@Configuration
@Profile(AppProfiles.mail)
class MailGatewayConfig {

  @Bean
  @ConditionalOnProperty(
    value = ["MAILGUN_KEY"],
    matchIfMissing = false,
  )
  fun mailGunService(
    @Value("MAILGUN_KEY") key: String,
    mailGatewayProperties: MailGatewayProperties
  ): MailGunGateway {
    return MailGunGateway(
      MailgunClient.config(key)
        .createApi(MailgunMessagesApi::class.java),
      mailGatewayProperties
    )
  }

  @Bean
  @ConditionalOnMissingBean(MailGateway::class)
  fun nativeMailService(
    javaMailSender: JavaMailSender,
    mailGatewayProperties: MailGatewayProperties
  ): MailGateway {
    return NativeMailGateway(javaMailSender, mailGatewayProperties)
  }
}
