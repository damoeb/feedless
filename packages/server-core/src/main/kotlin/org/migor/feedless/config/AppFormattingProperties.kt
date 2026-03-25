package org.migor.feedless.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppFormattingProperties {
  lateinit var dateFormat: String
  lateinit var timeFormat: String
  lateinit var webToFeedVersion: String
  lateinit var timezone: String
}
