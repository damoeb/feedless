package org.migor.feedless.api.http

import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.WebToFeedParamsV1
import org.migor.feedless.feed.discovery.FeedDiscoveryService
import org.migor.feedless.feed.discovery.TransientOrExistingNativeFeed
import org.migor.feedless.service.PropertyService
import org.migor.feedless.web.GenericFeedRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

data class LegacyAppFeatureFlags(
  val canPrerender: Boolean,
  val stateless: Boolean,
)

data class LegacyAppSettings(
  val flags: LegacyAppFeatureFlags,
  val webToFeedVersion: String,
  val urls: Map<String, String>,
)

data class LegacyDiscoveryOptions(
  val harvestUrl: String,
  val originalUrl: String,
  val withJavaScript: Boolean = false
)

data class LegacyNativeFeed(
  val url: String,
  val type: String,
  val title: String
)

data class LegacyArticleSample(
  val id: String,
  val title: String,
  val tags: String?,
  val contentText: String,
  val contentRaw: String,
  val contentRawMime: String,
  val imageUrl: String?,
  val url: String,
  val author: String?,
  val enclosures: String?,
  val publishedAt: String, //"2023-05-25T20:30:30.416+00:00",
  val commentsFeedUrl: String? = null
)

data class LegacyGenericFeed(
  val url: String,
  val linkXPath: String,
  val extendContext: String,
  val contextXPath: String,
  val dateXPath: String?,
  val feedUrl: String, // "http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fheise.de&linkXPath=.%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle",
  val count: Int?,
  val score: Double,
  val sample: List<LegacyArticleSample>
)

data class LegacyDiscoveryResults(
  val relatedFeeds: List<String> = emptyList(),
  val screenshot: String = "",
  val genericFeedRules: List<LegacyGenericFeed>,
  val nativeFeeds: List<LegacyNativeFeed>,
  val mimeType: String?,
  val body: String?,
  val failed: Boolean,
  val errorMessage: String?
)

data class LegacyDiscovery(val options: LegacyDiscoveryOptions, val results: LegacyDiscoveryResults)

/**
 * To support the rss-proxy ui
 */
@Controller
@Profile(AppProfiles.legacySupport)
class LegacyController {

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var webToFeedController: WebToFeedController

  @Autowired
  lateinit var feedEndpoint: FeedEndpoint

  @Autowired
  lateinit var environment: Environment

  @Throttled
  @Timed
  @GetMapping("/api/legacy/w2f")
  fun legacyWebToFeed(
    @RequestParam(WebToFeedParamsV1.url) url: String,
    @RequestParam(WebToFeedParamsV1.link, required = false) linkXPath: String?,
    @RequestParam(WebToFeedParamsV1.linkAlias, required = false) linkXPathAlias: String?,
    @RequestParam(WebToFeedParamsV1.extendContext, required = false) extendContext: String?,
    @RequestParam(WebToFeedParamsV1.contextPath) contextXPath: String,
    @RequestParam(WebToFeedParamsV1.datePath, required = false) dateXPath: String?,
//    @RequestParam("re", required = false) articleRecovery: String?,
//    @RequestParam("pp", required = false, defaultValue = "false") prerender: Boolean,
//    @RequestParam("ppS", required = false) puppeteerScript: String?,
    @RequestParam(WebToFeedParamsV1.debug, required = false) debug: Boolean?,
    @RequestParam(WebToFeedParamsV1.filter) filter: String?,
    @RequestParam(WebToFeedParamsV1.version) version: String,
    @RequestParam(WebToFeedParamsV1.format, required = false) responseTypeParam: String?,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    return webToFeedController.webToFeed(
      null,
      url,
      StringUtils.trimToNull(linkXPath) ?: linkXPathAlias!!,
      extendContext,
      contextXPath,
      debug ?: false,
      filter ?: "",
      "",
      dateXPath,
      false,
      false,
      false,
      null,
      null,
      "0.1",
      null,
      "",
      "",
      "",
      "",
      null,
      responseTypeParam,
      request
    )
  }

  @Throttled
  @Timed
  @GetMapping("/api/legacy/tf")
  fun transformFeed(
    @RequestParam(WebToFeedParamsV1.url) feedUrl: String,
    @RequestParam(WebToFeedParamsV1.filter, required = false) filter: String?,
//    @RequestParam("re", required = false) articleRecoveryParam: String?,
    @RequestParam(WebToFeedParamsV1.debug, required = false) debug: Boolean?,
    @RequestParam(WebToFeedParamsV1.format, required = false, defaultValue = "json") targetFormat: String,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    return feedEndpoint.transformFeed(
      feedUrl, filter, "", targetFormat, debug ?: false, request
    )
  }

//  @Throttled
//  @GetMapping("/api/legacy/discover")
//  fun discover(
//    @RequestParam(
//      WebToFeedParamsV1.homepageUrl,
//      required = false,
//      defaultValue = ""
//    ) homepageUrl: String
//  ): ResponseEntity<LegacyDiscovery> {
//
//    val scrapeRequest = ScrapeRequest.newBuilder()
//      .page(ScrapePage.newBuilder()
//        .url(homepageUrl)
//        .build())
//      .build()
//    val discovery = feedDiscoveryService.discoverFeeds("-", scrapeRequest)
//    return ResponseEntity.ok(
//      LegacyDiscovery(
//        options = LegacyDiscoveryOptions(
//          harvestUrl = discovery.options.harvestUrl,
//          originalUrl = discovery.options.originalUrl,
//        ),
//        results = LegacyDiscoveryResults(
//          genericFeedRules = discovery.results.genericFeedRules.map { toLegacyGenericFeed(it) },
//          nativeFeeds = discovery.results.nativeFeeds.map { toLegacyNativeFeed(it) },
//          mimeType = discovery.results.document.mimeType,
//          body = discovery.results.document.body,
//          failed = discovery.results.failed,
//          errorMessage = discovery.results.errorMessage
//        )
//      )
//    )
//  }

  private fun toLegacyGenericFeed(genericFeedRule: GenericFeedRule): LegacyGenericFeed {
    return LegacyGenericFeed(
      url = genericFeedRule.feedUrl,
      linkXPath = genericFeedRule.linkXPath,
      extendContext = "",
      contextXPath = genericFeedRule.contextXPath,
      dateXPath = genericFeedRule.dateXPath,
      feedUrl = genericFeedRule.feedUrl, // "http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fheise.de&linkXPath=.%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle",
      count = genericFeedRule.count,
      score = genericFeedRule.score,
      sample = emptyList()
    )
  }

  private fun toLegacyNativeFeed(transientOrExistingNativeFeed: TransientOrExistingNativeFeed): LegacyNativeFeed {
    return LegacyNativeFeed(
      url = transientOrExistingNativeFeed.transient!!.url,
      type = transientOrExistingNativeFeed.transient.type.name,
      title = transientOrExistingNativeFeed.transient.title

    )
  }

  @Throttled
  @GetMapping("/api/legacy/auth")
  fun auth(): ResponseEntity<String> {
    return ResponseEntity.ok("{\"maxAge\":600}")
  }

  @Throttled
  @GetMapping("/api/legacy/settings")
  fun settings(): ResponseEntity<LegacyAppSettings> {
    val appSettings = LegacyAppSettings(
      flags = LegacyAppFeatureFlags(
        canPrerender = false,
        stateless = environment.acceptsProfiles(Profiles.of("!${AppProfiles.database}")),
      ),
      urls = getApiUrls(),
      webToFeedVersion = "0.1",
    )
    return ResponseEntity.ok(appSettings)
  }

  private fun getApiUrls(): Map<String, String> = mapOf(
  "transformFeed" to "/api/tf",
  "discoverFeeds" to "/api/legacy/discover",
  "webToFeed" to "/api/w2f",
  "host" to propertyService.apiGatewayUrl
  )
}
