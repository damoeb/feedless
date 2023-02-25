package org.migor.rich.rss.service

import jakarta.annotation.PostConstruct
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
  lateinit var publicUrl: String
  lateinit var nitterHost: String
  lateinit var puppeteerHost: String
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
    logProperty("publicUrl = $publicUrl")
    domain = URL(publicUrl).host
//    logProperty("nitterHost = $nitterHost")
//    logProperty("invidiousHost = $invidiousHost")
    logProperty("dateFormat = $dateFormat")
    logProperty("timeFormat = $timeFormat")
    logProperty("webToFeedVersion = $webToFeedVersion")
    logProperty("puppeteerHost = $puppeteerHost")
    logProperty("timezone = $timezone")
    locale = Locale.forLanguageTag(defaultLocale)
    logProperty("locale = $locale")
    Assert.hasLength(jwtSecret, "jwtSecret must not be empty")
    Assert.hasLength(publicUrl, "publicUrl must not be empty")
  }

  private fun logProperty(value: String) {
    log.info("property ${value}")
  }
}
