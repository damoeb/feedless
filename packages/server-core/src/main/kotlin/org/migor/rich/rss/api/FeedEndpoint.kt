package org.migor.rich.rss.api

import io.micrometer.core.annotation.Timed
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.FeedDiscovery
import org.migor.rich.rss.api.dto.FeedDiscoveryOptions
import org.migor.rich.rss.api.dto.FeedDiscoveryResults
import org.migor.rich.rss.api.dto.PermanentFeedUrl
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.discovery.FeedReference
import org.migor.rich.rss.discovery.GenericFeedLocator
import org.migor.rich.rss.discovery.NativeFeedLocator
import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.harvest.feedparser.FeedType
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.AnnouncementService
import org.migor.rich.rss.service.AuthConfig
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.PuppeteerService
import org.migor.rich.rss.transform.GenericFeedRule
import org.migor.rich.rss.transform.WebToFeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.FeedUtil
import org.migor.rich.rss.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@RestController
class FeedEndpoint {

  private val log = LoggerFactory.getLogger(FeedEndpoint::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var genericFeedLocator: GenericFeedLocator

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @Autowired
  lateinit var articleRecovery: ArticleRecovery

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var announcementService: AnnouncementService

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var feedExporter: FeedExporter

  //  @RateLimiter(name="processService", fallbackMethod = "processFallback")
  @Throttled
  @Timed
  @GetMapping(ApiUrls.discoverFeeds)
  fun discoverFeeds(
    @RequestParam("homepageUrl") homepageUrl: String,
    @RequestParam("script", required = false) script: String?,
    @RequestParam( ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam("prerender", defaultValue = "false") prerender: Boolean,
    @CookieValue(AuthConfig.tokenCookie) token: String,
    request: HttpServletRequest
  ): FeedDiscovery {
    val corrId = handleCorrId(corrIdParam)
    fun buildDiscoveryResponse(
      url: String,
      mimeType: MimeType?,
      nativeFeeds: List<FeedReference>,
      relatedFeeds: List<Feed>,
      genericFeedRules: List<GenericFeedRule> = emptyList(),
      body: String = "",
      screenshot: String? = "",
      failed: Boolean = false,
      errorMessage: String? = null
    ): FeedDiscovery {
      return FeedDiscovery(
        options = FeedDiscoveryOptions(
          harvestUrl = url,
          originalUrl = homepageUrl,
          withJavaScript = prerender,
        ),
        results = FeedDiscoveryResults(
          mimeType = mimeType?.toString(),
          screenshot = screenshot,
          nativeFeeds = nativeFeeds,
          relatedFeeds = relatedFeeds,
          genericFeedRules = genericFeedRules,
          body = body,
          failed = failed,
          errorMessage = errorMessage
        )
      )
    }
    log.info("[$corrId] feeds/discover url=$homepageUrl, prerender=$prerender")
    return runCatching {
      authService.validateAuthToken(corrId, token, request.remoteAddr)
      val url = httpService.parseUrl(homepageUrl)

      httpService.guardedHttpResource(corrId, url, 200, listOf("text/"))
      val staticResponse = httpService.httpGet(corrId, url, 200)

      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(staticResponse)

      val relatedFeeds = feedService.findRelatedByUrl(url)
      if (feedType !== FeedType.NONE) {
        val feed = feedService.parseFeed(corrId, HarvestResponse(url, staticResponse))
        log.info("[$corrId] is native-feed")
        buildDiscoveryResponse(
          url,
          mimeType,
          relatedFeeds = relatedFeeds,
          nativeFeeds = listOf(FeedReference(url = url, type = feedType, title = feed.title))
        )
      } else {
        if (prerender) {
          val puppeteerResponse = puppeteerService.prerender(corrId, url, StringUtils.trimToEmpty(script))
          val (nativeFeeds, genericFeedRules) = extractFeeds(corrId, puppeteerResponse.html!!, url)
          buildDiscoveryResponse(
            url, mimeType,
            nativeFeeds = nativeFeeds,
            relatedFeeds = relatedFeeds,
            genericFeedRules = genericFeedRules,
            body = puppeteerResponse.html,
            screenshot = puppeteerResponse.screenshot,
            errorMessage = puppeteerResponse.errorMessage
          )
        } else {
          val body = String(staticResponse.responseBody)
          val (nativeFeeds, genericFeedRules) = extractFeeds(corrId, body, url)
          buildDiscoveryResponse(
            url, mimeType,
            nativeFeeds = nativeFeeds,
            relatedFeeds = relatedFeeds,
            genericFeedRules = genericFeedRules,
            body = body
          )
        }
      }
    }.getOrElse {
      log.error("[$corrId] Unable to discover feeds: ${it.message}")
      // todo mag return error code
      buildDiscoveryResponse(
        url = homepageUrl,
        nativeFeeds = emptyList(),
        relatedFeeds = emptyList(),
        mimeType = null,
        failed = true,
        errorMessage = it.message
      )
    }
  }

  @Throttled
  @GetMapping(ApiUrls.standaloneFeed)
  fun standaloneFeed(
    @RequestParam("url") feedUrl: String,
    @RequestParam( ApiParams.corrId, required = false) corrIdParam: String?,
    @CookieValue(AuthConfig.tokenCookie) token: String,
    request: HttpServletRequest,
  ): PermanentFeedUrl {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] feeds/to-permanent url=$feedUrl")
    authService.validateAuthToken(corrId, token, request.remoteAddr)
    return authService.requestStandaloneFeedUrl(corrId, feedUrl, request)
  }

  private fun extractFeeds(
    corrId: String,
    html: String,
    url: String
  ): Pair<List<FeedReference>, List<GenericFeedRule>> {
    val document = HtmlUtil.parse(html)
    val genericFeedRules = genericFeedLocator.locateInDocument(corrId, document, url)
    val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
    log.info("[$corrId] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size}")
    return Pair(nativeFeeds, genericFeedRules)
  }

  @Throttled
  @Timed
  @GetMapping("/api/feeds/transform", ApiUrls.transformFeed)
  fun transformFeed(
    @RequestParam("url") feedUrl: String,
    @RequestParam("q", required = false) filter: String?,
    @RequestParam("re", required = false) articleRecoveryParam: String?,
    @RequestParam( ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam("out", required = false, defaultValue = "json") targetFormat: String,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    val corrId = handleCorrId(corrIdParam)
    val articleRecovery = articleRecovery.resolveArticleRecovery(articleRecoveryParam)
    log.info("[$corrId] feeds/transform feedUrl=$feedUrl articleRecovery=$articleRecovery")
    val token = authService.interceptToken(request)
    val selfUrl = createFeedUrlFromTransform(feedUrl, filter, articleRecovery, targetFormat, token)
    return runCatching {
      val decoded = authService.validateAuthToken(corrId, request)
      val feed = feedService.parseFeedFromUrl(corrId, feedUrl)
      feed.feed_url = selfUrl
      feed.items = feed.items.asSequence()
        .filterIndexed { index, _ -> this.articleRecovery.shouldRecover(articleRecovery, index) }
        .map { this.articleRecovery.recoverAndMerge(corrId, it, articleRecovery) }
        .filter { filterService.matches(corrId, it, filter) }
        .toList()
        .plus(announcementService.byToken(corrId, decoded, selfUrl))

      feedExporter.to(corrId, targetFormat, feed, 20.toLong().toDuration(DurationUnit.MINUTES))
    }.getOrElse {
      if (it is HostOverloadingException) {
        throw it
      }
      log.error("[$corrId] $it")
      val article = webToFeedService.createMaintenanceArticle(it, feedUrl)
      feedExporter.to(corrId, targetFormat, webToFeedService.createMaintenanceFeed(corrId, feedUrl, selfUrl, article), 1.toLong().toDuration(DurationUnit.DAYS))
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
    return "${propertyService.publicUrl}${ApiUrls.transformFeed}?feedUrl=${encode(feedUrl)}&filter=${encode(StringUtils.trimToEmpty(filter))}&recovery=${encode(recovery.name)}&targetFormat=${encode(targetFormat)}&token=${encode(token)}"
  }

  @Throttled
  @Timed
  @GetMapping(ApiUrls.explainFeed)
  fun explainFeed(
    @RequestParam("feedUrl") feedUrl: String,
    @RequestParam( ApiParams.corrId, required = false) corrIdParam: String?,
    @CookieValue(AuthConfig.tokenCookie) token: String,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] feeds/explain feedUrl=$feedUrl")
    return runCatching {
      authService.validateAuthToken(corrId, token, request.remoteAddr)
      val feed = feedService.parseFeedFromUrl(corrId, feedUrl)
      feedExporter.to(corrId, "json", feed)
    }.getOrElse {
      log.error("[$corrId] $it")
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
