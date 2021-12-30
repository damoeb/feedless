package org.migor.rss.rich.service

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
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

  @PostConstruct
  fun onInit() {
    log.info("host=${host}")
    log.info("nitterHost=${nitterHost}")
    log.info("invidiousHost=${invidiousHost}")
    log.info("dateFormat=${dateFormat}")
    log.info("timeFormat=${timeFormat}")
    log.info("webToFeedVersion=${webToFeedVersion}")
  }
}
