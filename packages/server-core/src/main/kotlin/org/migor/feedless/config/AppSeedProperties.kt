package org.migor.feedless.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppSeedProperties {
  var anonymousEmail: String = "anonymous@localhost"
  lateinit var rootEmail: String
  lateinit var rootSecretKey: String
}
