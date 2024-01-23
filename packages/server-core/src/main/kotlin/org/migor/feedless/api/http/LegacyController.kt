package org.migor.feedless.api.http

import io.micrometer.core.annotation.Timed
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.WebToFeedParamsV1
import org.migor.feedless.api.WebToFeedParamsV2
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.service.PropertyService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.FeedUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

//"http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fheise.de&linkXPath=.%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle",

/**
 * To support the rss-proxy ui
 */
@Controller
class LegacyController {

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @Autowired
  lateinit var environment: Environment

  @Timed
  @GetMapping(
    ApiUrls.legacyWebToFeed,
    ApiUrls.webToFeedVerbose,
    ApiUrls.webToFeed,
    ApiUrls.webToFeedFromRule,
    ApiUrls.webToFeedFromChange
  )
  fun legacyWebToFeed(
    @RequestParam(WebToFeedParamsV1.format, required = false) responseFormat: String?,
    @RequestParam(WebToFeedParamsV1.url, required = false) urlV1: String?,
    @RequestParam(WebToFeedParamsV2.url, required = false) urlV2: String?,
  ): ResponseEntity<String> {
    return eolMessage(responseFormat, urlV1 ?: urlV2)
  }

  @Timed
  @GetMapping(
    ApiUrls.legacyTransformFeed,
    ApiUrls.transformFeed
  )
  fun transformFeed(
    @RequestParam(WebToFeedParamsV1.format, required = false) responseFormat: String,
    @RequestParam(WebToFeedParamsV1.url, required = false) url: String?,
  ): ResponseEntity<String> {
    return eolMessage(responseFormat, url)
  }

  private fun eolMessage(responseType: String?, url: String?): ResponseEntity<String> {
    val (_, convert) = feedExporter.resolveResponseType(newCorrId(), StringUtils.trimToNull(responseType) ?: "atom")
    val feed = createEolFeed()
    feed.items = listOf(createEolArticle(url))
    return convert(
      feed,
      HttpStatus.SERVICE_UNAVAILABLE,
      1.toDuration(DurationUnit.DAYS)
    )
  }

  @GetMapping("/api/legacy/auth", "/api/legacy/settings")
  fun settings(): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.GONE).build()
  }

  fun createEolFeed(): RichFeed {
    val feed = RichFeed()
    feed.id = ""
    feed.title = ""
    feed.feedUrl = ""
    feed.expired = true
    feed.publishedAt = Date()
    return feed
  }

  fun createEolArticle(url: String?): RichArticle {
    val article = RichArticle()
    val migrationUrl = if (url == null) {
      propertyService.appHost
    } else {
      "${propertyService.appHost}?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}"
    }

    article.id = FeedUtil.toURI("end-of-life", migrationUrl)
    article.title = "Expired Feed Service"
    article.contentText = "This service has has ended. You may migrate to the latest version using this link $migrationUrl"
    article.url = migrationUrl
    article.publishedAt = Date()
    return article
  }

}
