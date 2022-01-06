package org.migor.rss.rich.service

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
@ConfigurationProperties("app")
class PropertyService {

  private val log = LoggerFactory.getLogger(PropertyService::class.simpleName)

  lateinit var host: String
  lateinit var nitterHost: String
  lateinit var invidiousHost: String
  lateinit var puppeteerHost: String
  lateinit var dateFormat: String
  lateinit var timeFormat: String
  lateinit var webToFeedVersion: String
  lateinit var timezone: String
  lateinit var locale: Locale
  lateinit var defaultLocale: String

  @PostConstruct
  fun onInit() {
    log.info("host=${host}")
    log.info("nitterHost=${nitterHost}")
    log.info("invidiousHost=${invidiousHost}")
    log.info("dateFormat=${dateFormat}")
    log.info("timeFormat=${timeFormat}")
    log.info("webToFeedVersion=${webToFeedVersion}")
    log.info("timezone=${timezone}")
    locale = Locale.forLanguageTag(defaultLocale)
    log.info("locale=${locale}")
  }
}
