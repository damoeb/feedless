package org.migor.feedless.api.http

import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.Throttled
import org.migor.feedless.feed.discovery.FeedDiscoveryService
import org.migor.feedless.feed.discovery.TransientOrExistingNativeFeed
import org.migor.feedless.service.PropertyService
import org.migor.feedless.web.FetchOptions
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
    @RequestParam("url") url: String,
    @RequestParam("link") linkXPath: String,
    @RequestParam("x", defaultValue = "") extendContext: String,
    @RequestParam("context") contextXPath: String,
    @RequestParam("date", required = false) dateXPath: String?,
    @RequestParam("re", required = false) articleRecovery: String?,
    @RequestParam("pp", required = false, defaultValue = "false") prerender: Boolean,
    @RequestParam("ppS", required = false) puppeteerScript: String?,
    @RequestParam("debug", required = false) debug: Boolean?,
    @RequestParam("q") filter: String?,
    @RequestParam("v") version: String,
    @RequestParam("out", required = false) responseTypeParam: String?,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    return webToFeedController.webToFeed(
      null,
      url,
      linkXPath,
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
      request
    )
  }

  @Throttled
  @Timed
  @GetMapping("/api/legacy/tf")
  fun transformFeed(
    @RequestParam("url") feedUrl: String,
    @RequestParam("q", required = false) filter: String?,
    @RequestParam("re", required = false) articleRecoveryParam: String?,
    @RequestParam("debug", required = false) debug: Boolean?,
    @RequestParam("out", required = false, defaultValue = "json") targetFormat: String,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    return feedEndpoint.transformFeed(
      feedUrl, filter, debug ?: false, null, targetFormat, request
    )
  }

  @Throttled
  @GetMapping("/api/legacy/discover")
  fun discover(
    @RequestParam(
      "homepageUrl",
      required = false,
      defaultValue = "0"
    ) homepageUrl: String
  ): ResponseEntity<LegacyDiscovery> {

    val fetchOptions = FetchOptions(homepageUrl)
    val discovery = feedDiscoveryService.discoverFeeds("-", fetchOptions)
    return ResponseEntity.ok(
      LegacyDiscovery(
        options = LegacyDiscoveryOptions(
          harvestUrl = discovery.options.harvestUrl,
          originalUrl = discovery.options.originalUrl,
        ),
        results = LegacyDiscoveryResults(
          genericFeedRules = discovery.results.genericFeedRules.map { toLegacyGenericFeed(it) },
          nativeFeeds = discovery.results.nativeFeeds.map { toLegacyNativeFeed(it) },
          mimeType = discovery.results.document.mimeType,
          body = discovery.results.document.body,
          failed = discovery.results.failed,
          errorMessage = discovery.results.errorMessage
        )
      )
    )
  }

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
  "transformFeed" to "/api/legacy/tf",
  "discoverFeeds" to "/api/legacy/discover",
  "webToFeed" to "/api/legacy/w2f",
  "host" to propertyService.apiGatewayUrl
  )
}
