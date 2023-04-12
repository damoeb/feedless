package org.migor.rich.rss.service

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

  private val log = LoggerFactory.getLogger(PropertyService::class.simpleName)
  lateinit var domain: String
  lateinit var apiGatewayUrl: String
  lateinit var nitterHost: String
  lateinit var invidiousHost: String
  lateinit var dateFormat: String
  lateinit var timeFormat: String
  lateinit var webToFeedVersion: String
  lateinit var timezone: String
  lateinit var locale: Locale
  lateinit var defaultLocale: String
  lateinit var jwtSecret: String
  lateinit var rootEmail: String
  lateinit var rootSecretKey: String

  @PostConstruct
  fun onInit() {
    domain = URL(apiGatewayUrl).host
    logProperty("apiGatewayUrl = $apiGatewayUrl ($domain)")
//    logProperty("nitterHost = $nitterHost")
//    logProperty("invidiousHost = $invidiousHost")
    logProperty("dateFormat = $dateFormat")
    logProperty("timeFormat = $timeFormat")
    logProperty("webToFeedVersion = $webToFeedVersion")
    logProperty("timezone = $timezone")
    logProperty("rootEmail = $rootEmail")
    logProperty("rootSecretKey = ${StringUtils.substring(rootSecretKey,0,4)}****")
    locale = Locale.forLanguageTag(defaultLocale)
    logProperty("locale = $locale")
    Assert.hasLength(jwtSecret, "jwtSecret must not be empty")
    Assert.hasLength(apiGatewayUrl, "publicUrl must not be empty")
    Assert.isTrue(!StringUtils.startsWith(rootSecretKey, "\${"), "rootSecretKey seems invalid. Provide env var ROOT_SECRET_KEY")
    Assert.isTrue(!StringUtils.startsWith(rootEmail, "\${"), "rootEmail '${rootEmail}' seems invalid. Provide env var ROOT_EMAIL")
  }

  private fun logProperty(value: String) {
    log.info("property ${value}")
  }
}
