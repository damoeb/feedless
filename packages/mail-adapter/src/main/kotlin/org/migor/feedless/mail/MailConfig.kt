package org.migor.feedless.mail

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(AppProfiles.mail)
class MailConfig {

//  @Bean
//  @ConditionalOnProperty(
//    value = ["MAILGUN_KEY"],
//    matchIfMissing = false,
//  )
//  fun mailgunMessagesApi(@Value("MAILGUN_KEY") key: String): MailgunMessagesApi {
//    return MailgunClient.config(key)
//      .createApi(MailgunMessagesApi::class.java)
//  }
}
