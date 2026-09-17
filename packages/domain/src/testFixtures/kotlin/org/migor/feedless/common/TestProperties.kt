package org.migor.feedless.common

fun testPublicUrls(
  apiGatewayUrl: String = "http://localhost:8080",
  appHost: String = "http://localhost:4200",
) = PublicUrls(apiGatewayUrl = apiGatewayUrl, appHost = appHost)

fun testLocaleProperties(
  defaultLocale: java.util.Locale = java.util.Locale.ENGLISH,
  timezone: String = "UTC",
) = LocaleProperties(defaultLocale = defaultLocale, timezone = timezone)
