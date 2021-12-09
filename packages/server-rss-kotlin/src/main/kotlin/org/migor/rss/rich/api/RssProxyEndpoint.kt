package org.migor.rss.rich.api

import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.service.RssProxyService
import org.migor.rss.rich.util.CryptUtil
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
class RssProxyEndpoint {

  private val log = LoggerFactory.getLogger(RssProxyEndpoint::class.simpleName)

  @Autowired
  lateinit var rssProxyService: RssProxyService

  @GetMapping("/api/rss-proxy", "/api/rss-proxy/atom")
  fun getFeedAtom(
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("excludeAnyUrlMatching", required = false) excludeAnyUrlMatching: String?,
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toAtom(
      rssProxyService.applyRule(
        url,
        linkXPath,
        contextXPath,
        extendContext,
        parseExcludeUrl(excludeAnyUrlMatching),
        toCorrelationId(corrId)
      )
    )
  }

  private fun toCorrelationId(corrId: String?): String {
    return Optional.ofNullable(corrId).orElse(CryptUtil.newCorrId());
  }

  @GetMapping("/api/rss-proxy/rss")
  fun getFeedRss(
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("excludeAnyUrlMatching", required = false) excludeAnyUrlMatching: String?,
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toRss(
      rssProxyService.applyRule(
        url,
        linkXPath,
        contextXPath,
        extendContext,
        parseExcludeUrl(excludeAnyUrlMatching),
        toCorrelationId(corrId)
      )
    )
  }

  @GetMapping("/api/rss-proxy/json")
  fun getFeedJson(
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("excludeAnyUrlMatching", required = false) excludeAnyUrlMatching: String?,
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toJson(
      rssProxyService.applyRule(
        url,
        linkXPath,
        contextXPath,
        extendContext,
        parseExcludeUrl(excludeAnyUrlMatching),
        toCorrelationId(corrId)
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
