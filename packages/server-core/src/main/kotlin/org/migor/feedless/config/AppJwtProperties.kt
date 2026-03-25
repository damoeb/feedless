package org.migor.feedless.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppJwtProperties {
  lateinit var jwtSecret: String
}
