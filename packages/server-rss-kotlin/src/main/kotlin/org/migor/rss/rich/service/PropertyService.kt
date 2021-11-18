package org.migor.rss.rich.service

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class PropertyService {

  private val log = LoggerFactory.getLogger(PropertyService::class.simpleName)

  // todo mag fix @value
//  @Value("#{environment.HOST}")
  var host: String? = null

  fun host(): String = Optional.ofNullable(StringUtils.trimToNull(host)).orElse("http://localhost:8080")

  fun rssProxyUrl(): String = "http://localhost:4200"
  fun nitterUrl(): String = "http://localhost:8000"
  fun publicHost(): String = "http://localhost:8080"
  fun getPuppeteerHost(): String = "http://localhost:3000"
}
