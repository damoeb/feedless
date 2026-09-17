package org.migor.feedless.common

import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated
import java.util.Locale

/** The locale and timezone this instance falls back to when a document carries none. */
@ConfigurationProperties("app")
@Validated
@Profile(AppProfiles.properties)
data class LocaleProperties(
  val defaultLocale: Locale,
  val timezone: String = "UTC",
)
