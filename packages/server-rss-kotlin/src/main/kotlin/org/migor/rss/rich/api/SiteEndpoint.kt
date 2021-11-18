package org.migor.rss.rich.api

import org.migor.rss.rich.service.SiteService
import org.migor.rss.rich.service.YtDlResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SiteEndpoint {

  private val log = LoggerFactory.getLogger(SiteEndpoint::class.simpleName)

  @Autowired
  lateinit var siteService: SiteService

  @GetMapping("/api/site/detect")
  fun detectEnclosures(
    @RequestParam("url") url: String,
  ): YtDlResult {
    return siteService.detectEnclosures(url)
  }
}
