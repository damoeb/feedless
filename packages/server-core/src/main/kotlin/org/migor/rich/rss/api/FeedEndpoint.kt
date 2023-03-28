package org.migor.rich.rss.api

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.FeedDiscovery
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.AuthConfig
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.transform.PuppeteerWaitUntil
import org.migor.rich.rss.transform.WebToFeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
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
  lateinit var feedService: FeedService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var articleRecovery: ArticleRecovery

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @Throttled
  @Timed
  @GetMapping(ApiUrls.discoverFeeds)
  suspend fun discoverFeeds(
    @RequestParam("homepageUrl") homepageUrl: String,
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam("strictMode", defaultValue = "false") strictMode: Boolean,
    @RequestParam("script", required = false) script: String?,
    @RequestParam("prerender", defaultValue = "false") prerender: Boolean,
    @CookieValue(AuthConfig.tokenCookie) token: String,
    request: HttpServletRequest
  ): FeedDiscovery {
    meterRegistry.counter("feeds/discover").increment()
    val corrId = handleCorrId(corrIdParam)

    log.info("[$corrId] feeds/discover url=$homepageUrl, prerender=$prerender, strictMode=$strictMode")
    authService.decodeToken(corrId, token)

    val fetchOptions = GenericFeedFetchOptions(
      websiteUrl = homepageUrl,
      prerender = prerender,
      prerenderWaitUntil = PuppeteerWaitUntil.load,
      prerenderWithoutMedia = false,
      prerenderScript = StringUtils.trimToEmpty(script)
    )

    return feedDiscovery.discoverFeeds(corrId, fetchOptions)
  }

//  @Throttled
//  @GetMapping(ApiUrls.standaloneFeed)
//  fun standaloneFeed(
//    @RequestParam("url") feedUrl: String,
//    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
//    @CookieValue(AuthConfig.tokenCookie) token: String,
//    request: HttpServletRequest,
//  ): PermanentFeedUrl {
//    val corrId = handleCorrId(corrIdParam)
//    log.info("[$corrId] feeds/to-permanent url=$feedUrl")
//    authService.validateAuthToken(corrId, token)
//    return authService.requestStandaloneFeedUrl(corrId, feedUrl, request)
//  }

  @Throttled
  @Timed
  @GetMapping("/api/feeds/transform", ApiUrls.transformFeed)
  fun transformFeed(
    @RequestParam("url") feedUrl: String,
    @RequestParam("q", required = false) filter: String?,
    @RequestParam("re", required = false) articleRecoveryParam: String?,
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam("out", required = false, defaultValue = "json") targetFormat: String,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    meterRegistry.counter("feeds/transform").increment()
    val corrId = handleCorrId(corrIdParam)
    val articleRecovery = articleRecovery.resolveArticleRecovery(articleRecoveryParam)
    log.info("[$corrId] feeds/transform feedUrl=$feedUrl articleRecovery=$articleRecovery filter=$filter")
    val token = authService.interceptToken(corrId, request)
    val selfUrl = createFeedUrlFromTransform(feedUrl, filter, articleRecovery, targetFormat, token)
    return runCatching {
      val feed = feedService.parseFeedFromUrl(corrId, feedUrl)
      feed.feedUrl = selfUrl
      feed.items = feed.items.asSequence()
        .filterIndexed { index, _ -> this.articleRecovery.shouldRecover(articleRecovery, index) }
        .map { this.articleRecovery.recoverAndMerge(corrId, it, articleRecovery) }
        .filter { filterService.matches(corrId, it, filter) }
        .toList()

      feedExporter.to(corrId, HttpStatus.OK, targetFormat, feed, 20.toLong().toDuration(DurationUnit.MINUTES))
    }.getOrElse {
      if (it is HostOverloadingException) {
        throw it
      }
      log.error("[$corrId] ${it.message}")
      val article = webToFeedService.createMaintenanceArticle(it, feedUrl)
      feedExporter.to(
        corrId,
        HttpStatus.SERVICE_UNAVAILABLE,
        targetFormat,
        webToFeedService.createMaintenanceFeed(corrId, feedUrl, selfUrl, article),
        1.toLong().toDuration(DurationUnit.DAYS)
      )
    }
  }

  private fun createFeedUrlFromTransform(
    feedUrl: String,
    filter: String?,
    recovery: ArticleRecoveryType,
    targetFormat: String,
    token: String
  ): String {
    val encode: (value: String) -> String = { value -> URLEncoder.encode(value, StandardCharsets.UTF_8) }
    return "${propertyService.publicUrl}${ApiUrls.transformFeed}?feedUrl=${encode(feedUrl)}&filter=${
      encode(
        StringUtils.trimToEmpty(
          filter
        )
      )
    }&recovery=${encode(recovery.name)}&targetFormat=${encode(targetFormat)}&token=${encode(token)}"
  }

  @Throttled
  @Timed
  @GetMapping(ApiUrls.explainFeed)
  fun explainFeed(
    @RequestParam("feedUrl") feedUrl: String,
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @CookieValue(AuthConfig.tokenCookie) token: String
  ): ResponseEntity<String> {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] feeds/explain feedUrl=$feedUrl")
    return runCatching {
      authService.decodeToken(corrId, token)
      val feed = feedService.parseFeedFromUrl(corrId, feedUrl)
      feedExporter.to(corrId, HttpStatus.OK, "json", feed)
    }.getOrElse {
      log.error("[$corrId] ${it.message}")
      ResponseEntity.badRequest().body(it.message)
    }
  }

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
