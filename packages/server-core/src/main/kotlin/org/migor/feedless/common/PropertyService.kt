package org.migor.feedless.common

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import java.net.URL
import java.util.*

@Service
@ConfigurationProperties("app")
class PropertyService {

  val anonymousEmail: String = "anonymous@localhost"

  private val log = LoggerFactory.getLogger(PropertyService::class.simpleName)
  lateinit var domain: String
  lateinit var apiGatewayUrl: String
  lateinit var appHost: String
  lateinit var dateFormat: String
  lateinit var timeFormat: String
  lateinit var webToFeedVersion: String
  lateinit var timezone: String
  lateinit var locale: Locale
  lateinit var defaultLocale: String
  lateinit var jwtSecret: String
  lateinit var rootEmail: String
  var schemaVersion: Int = 0
  lateinit var rootSecretKey: String

  @PostConstruct
  fun onInit() {
    domain = URL(apiGatewayUrl).host
    logProperty("apiGatewayUrl = $apiGatewayUrl ($domain)")
    logProperty("appHost = $appHost")
    logProperty("dateFormat = $dateFormat")
    logProperty("timeFormat = $timeFormat")
    logProperty("webToFeedVersion = $webToFeedVersion")
    logProperty("timezone = $timezone")
    logProperty("rootEmail = $rootEmail")
    logProperty("rootSecretKey = ${mask(rootSecretKey)}")
    logProperty("jwtSecret = ${mask(jwtSecret)}")
    locale = Locale.forLanguageTag(defaultLocale)
    logProperty("locale = $locale")

    Assert.hasLength(jwtSecret, "jwtSecret must not be empty")
    Assert.hasLength(apiGatewayUrl, "publicUrl must not be empty")
    Assert.isTrue(StringUtils.length(jwtSecret) >= jwtSecretMinLength, "jwtSecret too short (min length )")
    Assert.isTrue(!StringUtils.startsWith(jwtSecret, "\${"), "jwtSecret seems invalid")
    Assert.isTrue(
      StringUtils.length(rootSecretKey) >= rootSecretKeyMinLength,
      "jwtSecret too short (min length $rootSecretKeyMinLength)"
    )
    Assert.isTrue(
      !StringUtils.startsWith(rootSecretKey, "\${"),
      "rootSecretKey seems invalid. Provide env var APP_ROOT_SECRET_KEY"
    )
    Assert.isTrue(
      !StringUtils.startsWith(rootEmail, "\${"),
      "rootEmail '${rootEmail}' seems invalid. Provide env var APP_ROOT_EMAIL"
    )
  }

  private fun logProperty(value: String) {
    log.info("property $value")
  }

  private fun mask(value: String): String {
    return "${StringUtils.substring(value, 0, 4)}**** [masked]"
  }

  companion object {
    val maxPageSize: Int = 10
    val jwtSecretMinLength: Int = 10
    val rootSecretKeyMinLength: Int = 7
  }
}
