package org.migor.feedless.api.http

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.WebToFeedParamsV1
import org.migor.feedless.api.auth.IAuthService
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.harvest.HostOverloadingException
import org.migor.feedless.service.FeedParserService
import org.migor.feedless.service.FilterService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.migor.feedless.web.WebToFeedService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@RestController
class FeedEndpoint {

  private val log = LoggerFactory.getLogger(FeedEndpoint::class.simpleName)

  @Autowired
  lateinit var feedParserService: FeedParserService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @Autowired
  lateinit var authService: IAuthService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @Autowired
  lateinit var propertyService: PropertyService

//  @Autowired
//  lateinit var feedDiscovery: FeedDiscoveryService

//  @Throttled
//  @Timed
//  @GetMapping(ApiUrls.discoverFeeds)
//  suspend fun discoverFeeds(
//    @RequestParam("homepageUrl") homepageUrl: String,
//    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
//    @RequestParam("strictMode", defaultValue = "false") strictMode: Boolean,
//    @RequestParam("script", required = false) script: String?,
//    @RequestParam("prerender", defaultValue = "false") prerender: Boolean,
//    @CookieValue(AuthConfig.tokenCookie) token: String,
//    request: HttpServletRequest
//  ): FeedDiscovery {
//    meterRegistry.counter(AppMetrics.feedDiscovery).increment()
//    val corrId = handleCorrId(corrIdParam)
//
//    log.info("[$corrId] feeds/discover url=$homepageUrl, prerender=$prerender, strictMode=$strictMode")
//    authService.decodeToken(token)!!
//
//    val scrapeRequest = ScrapeRequest.newBuilder()
//      .page(
//        ScrapePage.newBuilder()
//        .url(homepageUrl)
//          .prerender(if(prerender) {
//            ScrapePrerender.newBuilder()
//              .evalScript(StringUtils.trimToEmpty(script))
//              .waitUntil(DtoResolver.toDto(PuppeteerWaitUntil.load))
//              .build()
//          } else {
//            null
//          })
//        .build())
//      .build()
//
//    return feedDiscovery.discoverFeeds(corrId, scrapeRequest)
//  }

  @Throttled
  @Timed
  @GetMapping("/api/feeds/transform", ApiUrls.transformFeed)
  fun transformFeed(
    @RequestParam(WebToFeedParamsV1.url) feedUrl: String,
    @RequestParam(WebToFeedParamsV1.filter, required = false) filter: String?,
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam(WebToFeedParamsV1.format, required = false, defaultValue = "json") targetFormat: String,
    @RequestParam(WebToFeedParamsV1.debug, required = false) debug: Boolean?,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    meterRegistry.counter(AppMetrics.feedTransform).increment()
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] feeds/transform feedUrl=$feedUrl filter=$filter")
    val jwt = authService.interceptJwt(request)
    val selfUrl = createFeedUrlFromTransform(feedUrl, filter, targetFormat, jwt)
    return runCatching {
      val feed = feedParserService.parseFeedFromUrl(corrId, feedUrl)
      feed.feedUrl = selfUrl
      feed.items = feed.items.asSequence()
        .filter { filterService.matches(corrId, it, filter) }
        .toList()

      feedExporter.to(corrId, HttpStatus.OK, targetFormat, feed, 20.toLong().toDuration(DurationUnit.MINUTES))
    }.getOrElse {
      if (it is HostOverloadingException) {
        throw it
      }
      log.error("[$corrId] ${it.message}")
      if (debug == true) {
        val article = webToFeedService.createMaintenanceArticle(it, feedUrl)
        feedExporter.to(
          corrId,
          HttpStatus.SERVICE_UNAVAILABLE,
          targetFormat,
          webToFeedService.createMaintenanceFeed(corrId, feedUrl, selfUrl, article),
          1.toLong().toDuration(DurationUnit.DAYS)
        )
      } else {
        ResponseEntity.badRequest().body(it.message)
      }
    }
  }

  private fun createFeedUrlFromTransform(
    feedUrl: String,
    filter: String?,
    targetFormat: String,
    jwt: Jwt?
  ): String {
    val encode: (value: String) -> String = { value -> URLEncoder.encode(value, StandardCharsets.UTF_8) }
    return "${propertyService.apiGatewayUrl}${ApiUrls.transformFeed}?feedUrl=${encode(feedUrl)}&filter=${
      encode(
        StringUtils.trimToEmpty(
          filter
        )
      )
    }&targetFormat=${encode(targetFormat)}${jwt?.let { "&token=${encode(jwt.tokenValue)}" }}"
  }

//  @Throttled
//  @Timed
//  @GetMapping(ApiUrls.explainFeed)
//  fun explainFeed(
//    @RequestParam("feedUrl") feedUrl: String,
//    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
//    @CookieValue(AuthConfig.tokenCookie) token: String
//  ): ResponseEntity<String> {
//    val corrId = handleCorrId(corrIdParam)
//    log.info("[$corrId] feeds/explain feedUrl=$feedUrl")
//    return runCatching {
//      authService.decodeToken(token)
//      val feed = feedService.parseFeedFromUrl(corrId, feedUrl)
//      feedExporter.to(corrId, HttpStatus.OK, "json", feed)
//    }.getOrElse {
//      log.error("[$corrId] ${it.message}")
//      ResponseEntity.badRequest().body(it.message)
//    }
//  }

//  @GetMapping("/api/feeds/query")
//  fun queryFeeds(
//    @RequestParam("q") query: String,
//  ): ResponseEntity<String> {
//    val corrId = CryptUtil.newCorrId()
//    try {
//      feedService.queryViaEngines(query, token)
//      return ResponseEntity.ok("")
//    } catch (e: Exception) {
//      log.error("[$corrId] Failed feedFromQueryEngines $query", e)
//      return ResponseEntity.badRequest()
//        .build()
//    }
//  }

}
