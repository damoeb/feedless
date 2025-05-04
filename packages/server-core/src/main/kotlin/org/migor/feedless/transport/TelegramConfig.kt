package org.migor.feedless.transport

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(AppProfiles.telegram)
class TelegramConfig {
//  @Bean
//  @ConditionalOnProperty(prefix = "app.telegram", name = ["token"])
//  fun telegramProperties(): TelegramProperties {
//    return TelegramProperties()
//  }
}
