package org.migor.feedless.config

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer

@Configuration
@Profile(AppProfiles.mail)
class MailConfig {

  @Bean
  fun createFreeMarkerConfigurer(): FreeMarkerConfigurer {
    val configurer = FreeMarkerConfigurer()
    configurer.setTemplateLoaderPath("classpath:/mail-templates")
    configurer.setDefaultEncoding("UTF-8")
    return configurer
  }
}
