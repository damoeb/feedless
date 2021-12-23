package org.migor.rss.rich.api

import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.service.WebToFeedService
import org.migor.rss.rich.util.CryptUtil.handleCorrId
import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class WebToFeedEndpoint {

  private val log = LoggerFactory.getLogger(WebToFeedEndpoint::class.simpleName)

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @GetMapping("/api/web-to-feed/atom")
  fun atom(
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("excludeAnyUrlMatching", required = false) excludeAnyUrlMatching: String?,
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toAtom(
      webToFeedService.applyRule(
        url,
        linkXPath,
        contextXPath,
        extendContext,
        parseExcludeUrl(excludeAnyUrlMatching),
        handleCorrId(corrId)
      )
    )
  }

  @GetMapping("/api/web-to-feed/rss")
  fun rss(
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("excludeAnyUrlMatching", required = false) excludeAnyUrlMatching: String?,
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toRss(
      webToFeedService.applyRule(
        url,
        linkXPath,
        contextXPath,
        extendContext,
        parseExcludeUrl(excludeAnyUrlMatching),
        handleCorrId(corrId)
      )
    )
  }

  @GetMapping("/api/web-to-feed", "/api/web-to-feed/json")
  fun jsonAndDefault(
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("excludeAnyUrlMatching", required = false) excludeAnyUrlMatching: String?,
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toJson(
      webToFeedService.applyRule(
        url,
        linkXPath,
        contextXPath,
        extendContext,
        parseExcludeUrl(excludeAnyUrlMatching),
        handleCorrId(corrId)
      )
    )
  }

  private fun parseExcludeUrl(excludeAnyUrlMatchingJson: String?): List<String> {
    return Optional.ofNullable(StringUtils.trimToNull(excludeAnyUrlMatchingJson))
      .map { excludeAnyUrlMatching ->
        runCatching {
          JsonUtil.gson.fromJson<List<String>>(excludeAnyUrlMatching, List::class.java)
        }
          .getOrDefault(listOf(excludeAnyUrlMatching))
      }
      .orElse(emptyList())
      .filterIndexed { index, _ -> index < 4 }
  }
}
