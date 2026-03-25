package org.migor.feedless.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
  AppUrlsProperties::class,
  AppJwtProperties::class,
  AppSeedProperties::class,
  AppLocaleProperties::class,
  AppFormattingProperties::class,
)
class AppFeedlessPropertiesConfiguration
