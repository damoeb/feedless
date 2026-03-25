package org.migor.feedless.config

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

@ConfigurationProperties(prefix = "app")
class AppLocaleProperties {
  lateinit var defaultLocale: String
  lateinit var locale: Locale

  @PostConstruct
  fun onInit() {
    locale = Locale.forLanguageTag(defaultLocale)
  }
}
