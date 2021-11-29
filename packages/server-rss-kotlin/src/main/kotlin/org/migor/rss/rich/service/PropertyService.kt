package org.migor.rss.rich.service

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class PropertyService {

  private val log = LoggerFactory.getLogger(PropertyService::class.simpleName)

  @Value("#{environment.HOST}")
  var host: String? = null

  @Value("#{environment.nitterHost}")
  var nitterHost: String? = null

  @Value("#{environment.invidiousHost}")
  var invidiousHost: String? = null

  @Value("#{environment.puppeteerHost}")
  var puppeteerHost: String? = null

  @PostConstruct
  fun onInit() {
    host = Optional.ofNullable(StringUtils.trimToNull(host)).orElseGet { "http://localhost:8080" }
    nitterHost = Optional.ofNullable(StringUtils.trimToNull(nitterHost)).orElseGet { "http://localhost:8081" }
    invidiousHost = Optional.ofNullable(StringUtils.trimToNull(invidiousHost)).orElseGet { "http://localhost:8080" }
    puppeteerHost = Optional.ofNullable(StringUtils.trimToNull(puppeteerHost)).orElseGet { "http://localhost:3000" }

    log.info("host=${host}")
    log.info("nitterHost=${nitterHost}")
    log.info("invidiousHost=${invidiousHost}")
    log.info("puppeteerHost=${puppeteerHost}")
  }

  fun rssProxyUrl(): String = "http://localhost:4200"
}
