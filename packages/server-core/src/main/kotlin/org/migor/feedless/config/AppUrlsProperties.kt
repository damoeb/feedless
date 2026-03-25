package org.migor.feedless.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppUrlsProperties {
  lateinit var apiGatewayUrl: String
  lateinit var appHost: String
}
