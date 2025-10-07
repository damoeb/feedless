package org.migor.feedless.mail

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.client.MailgunClient
import org.migor.feedless.AppProfiles
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(AppProfiles.mail)
class MailConfig {

  @Bean
  @ConditionalOnProperty(
    value = ["MAILGUN_KEY"],
    matchIfMissing = false,
  )
  fun mailgunMessagesApi(@Value("MAILGUN_KEY") key: String): MailgunMessagesApi {
    return MailgunClient.config(key)
      .createApi(MailgunMessagesApi::class.java)
  }
}
