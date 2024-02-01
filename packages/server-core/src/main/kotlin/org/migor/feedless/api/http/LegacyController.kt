package org.migor.feedless.api.http

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

  @GetMapping(
//    "/stream/feed/{feedId}/atom",
//    "/stream/feed/{feedId}/atom.xml",
    "/stream/feed/**",
//    "/stream/bucket/{bucketId}/atom",
    "/stream/bucket/**",
//    "/feed:{feedId}/atom",
    "/feed/**",
    "/feed:**",
//    "/bucket:{bucketId}/atom"
    "/bucket/**",
    "/bucket:**",
  )

  fun legacyEntities(
  ): ResponseEntity<String> {
    return eolMessage("atom", null)
  }

  @GetMapping(
    ApiUrls.legacyWebToFeed,
    ApiUrls.webToFeedVerbose,
    ApiUrls.webToFeed,
    ApiUrls.webToFeedFromRule,
    ApiUrls.webToFeedFromChange,
  )

  fun legacyWebToFeed(
    @RequestParam(WebToFeedParamsV1.format, required = false) responseFormat: String?,
    @RequestParam(WebToFeedParamsV1.url, required = false) urlV1: String?,
    @RequestParam(WebToFeedParamsV2.url, required = false) urlV2: String?,
  ): ResponseEntity<String> {
    return eolMessage(responseFormat, urlV1 ?: urlV2)
  }

  @GetMapping(
    ApiUrls.legacyTransformFeed,
    ApiUrls.transformFeed
  )
  fun transformFeed(
    @RequestParam(WebToFeedParamsV1.format, required = false) responseFormat: String?,
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
      HttpStatus.OK,
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
    val preregistrationLink = if (url == null) {
      propertyService.appHost
    } else {
      "${propertyService.appHost}?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}"
    }

    article.id = FeedUtil.toURI("end-of-life", preregistrationLink)
    article.title = "Important Notice: Termination of Legacy RSS-Proxy/feedless! Feedless 1 is coming!"
    article.contentText = """Dear User,

I hope this message finds you well. I am writing to inform you of some changes regarding our services that may affect you.

As of now, I regret to terminate of our free services, RSS-Proxy and feedless Alpha. I understand the convenience and value these services may have provided, and we sincerely apologize for any inconvenience this may cause.

However, we're excited to share that we're transitioning towards a new and improved version: feedless 1 will offer a free version, providing you with similar functionalities to what you've been accustomed to with RSS-Proxy and feedless Alph including self-hostinga. We believe this upgrade will enhance your experience and better meet your needs.

You have the opportunity to register to the pre-launch waiting list (limited size) by following this link: $preregistrationLink. By preregistering, you'll be among the first to access the new service once they become available.

Should you have any questions, concerns, or require further assistance, please don't hesitate to reach out to me at feedlessapp@proton.me.

Thank you for your understanding and continued support as we strive to improve our services and cater to your needs better.

Best regards,

Markus

    """.trimIndent()
//    article.contentText = "Thanks for using rssproxy or feedless. I have terminated the service has has ended. You may migrate to the latest version using this link $migrationUrl"
    article.url = preregistrationLink
    article.publishedAt = Date()
    return article
  }

}
