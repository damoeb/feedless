package org.migor.rss.rich.api

import org.migor.rss.rich.service.RssProxyService
import org.migor.rss.rich.util.CryptUtil
import org.migor.rss.rich.util.FeedExporter
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
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toAtom(rssProxyService.applyRule(url, linkXPath, contextXPath, extendContext, toCorrelationId(corrId)))
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
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toRss(rssProxyService.applyRule(url, linkXPath, contextXPath, extendContext, toCorrelationId(corrId)))
  }

  @GetMapping("/api/rss-proxy/json")
  fun getFeedJson(
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("correlationId", required = false) corrId: String?
  ): ResponseEntity<String> {
    return FeedExporter.toJson(rssProxyService.applyRule(url, linkXPath, contextXPath, extendContext, toCorrelationId(corrId)))
  }
}
