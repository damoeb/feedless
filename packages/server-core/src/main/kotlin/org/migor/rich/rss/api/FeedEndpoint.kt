package org.migor.rich.rss.api

import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.EnclosureDto
import org.migor.rich.rss.api.dto.FeedDiscovery
import org.migor.rich.rss.api.dto.FeedDiscoveryOptions
import org.migor.rich.rss.api.dto.FeedDiscoveryResults
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.discovery.FeedReference
import org.migor.rich.rss.discovery.GenericFeedLocator
import org.migor.rich.rss.discovery.NativeFeedLocator
import org.migor.rich.rss.harvest.DeepArticleRecovery
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.harvest.feedparser.FeedType
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.FilterService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.PuppeteerService
import org.migor.rich.rss.transform.GenericFeedRule
import org.migor.rich.rss.transform.WebToFeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.FeedExporter
import org.migor.rich.rss.util.FeedUtil
import org.migor.rich.rss.util.FeedUtil.resolveArticleRecovery
import org.migor.rich.rss.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.time.Duration
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
  lateinit var deepArticleRecovery: DeepArticleRecovery

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  //  @RateLimiter(name="processService", fallbackMethod = "processFallback")
  @GetMapping("/api/feeds/discover")
  fun discoverFeeds(
    @RequestParam("homepageUrl") homepageUrl: String,
    @RequestParam("script", required = false) script: String?,
    @RequestParam("correlationId", required = false) correlationId: String?,
    @RequestParam(name = "prerender", defaultValue = "false") prerender: Boolean
  ): FeedDiscovery {
    val corrId = handleCorrId(correlationId)
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
    return try {
      val url = httpService.parseUrl(homepageUrl)

      // todo check unsupported mimeTypes, do a head call

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
          val body = staticResponse.responseBody
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
    } catch (e: Exception) {
      log.error("[$corrId] Unable to discover feeds: ${e.message}")
      // todo mag return error code
      buildDiscoveryResponse(
        url = homepageUrl,
        nativeFeeds = emptyList(),
        relatedFeeds = emptyList(),
        mimeType = null,
        failed = true,
        errorMessage = e.message
      )
    }
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

  //  @RateLimiter(name="processService", fallbackMethod = "processFallback")
  @GetMapping("/api/feeds/transform")
  fun transformFeed(
    @RequestParam("feedUrl") feedUrl: String,
    @RequestParam("filter", required = false) filter: String?,
    @RequestParam("recovery", required = false) articleRecoveryParam: String?,
    @RequestParam("correlationId", required = false) correlationId: String?,
    @RequestHeader("authorization", required = false) authHeader: String?,
    @RequestParam("targetFormat", required = false, defaultValue = "json") targetFormat: String
  ): ResponseEntity<String> {
    val corrId = handleCorrId(correlationId)
    val articleRecovery = resolveArticleRecovery(articleRecoveryParam)
    log.info("[$corrId] feeds/transform feedUrl=$feedUrl articleRecovery=$articleRecovery")
    val export = { feed: FeedJsonDto, retryAfter: Duration -> when (targetFormat.lowercase()) {
      "rss" -> FeedExporter.toRss(corrId, feed, retryAfter)
      "json" -> FeedExporter.toJson(corrId, feed, retryAfter)
      else -> FeedExporter.toAtom(corrId, feed, retryAfter)
    }}
    try {
      val syndFeed = feedService.parseFeedFromUrl(corrId, feedUrl, authHeader)
      val items = syndFeed.entries.asSequence()
        .filterNotNull()
        .filterIndexed { index, _ -> deepArticleRecovery.shouldRecover(articleRecovery, index) }
        .mapNotNull { toArticle(it) }
        .map { deepArticleRecovery.recoverArticle(corrId, it, articleRecovery) }
        .filter { filterService.matches(it, filter) }.toList()
      val feed = FeedJsonDto(
        id = syndFeed.link,
        name = syndFeed.title,
        icon = syndFeed.image?.url,
        description = syndFeed.description,
        home_page_url = syndFeed.link,
        date_published = syndFeed.publishedDate,
        items = items,
        feed_url = syndFeed.link,
        expired = false,
        tags = syndFeed.categories.map { category -> category.name },
      )
      val retryAfter = 20.toLong().toDuration(DurationUnit.MINUTES)
      return export(feed, retryAfter)
    } catch (e: Throwable) {
      return export(webToFeedService.createMaintenanceFeed(corrId, e, feedUrl, feedUrl), 1.toLong().toDuration(DurationUnit.DAYS))
    }
  }

//  @GetMapping("/api/feeds/query")
//  fun feedFromQueryEngines(
//    @RequestParam("q") query: String,
//    @RequestParam("token") token: String
//  ): ResponseEntity<String> {
//    val corrId = CryptUtil.newCorrId()
//    try {
//      feedService.queryViaEngines(query, token)
//      return ResponseEntity.ok("")
//    } catch (e: Exception) {
//      log.error("[$corrId] Failed feedFromQueryEngines $query", e)
//      return ResponseEntity.badRequest()
//        .header("Content-Type", "application/json")
//        .body(e.message)
//    }
//  }

  private fun toArticle(syndEntry: SyndEntry): ArticleJsonDto? {
    return try {
      val text = if (syndEntry.description == null) {
        syndEntry.contents.filter { syndContent -> syndContent.type.contains("text") }
          .map { syndContent -> syndContent.value }
          .firstOrNull()
          .toString()
      } else {
        syndEntry.description.value
      }

      val rawContent = syndEntry.contents.filter { syndContent -> syndContent.type.contains("html") }
      ArticleJsonDto(
        id = syndEntry.uri,
        title = syndEntry.title!!,
        tags = syndEntry.categories.map { syndCategory -> syndCategory.name }.toList(),
        content_text = Optional.ofNullable(text).orElse(""),
        content_raw = rawContent
          .map { syndContent -> syndContent.value }
          .firstOrNull(),
        content_raw_mime = rawContent
          .map { syndContent -> syndContent.type }
          .firstOrNull(),
        url = syndEntry.link,
        author = syndEntry.author,
        enclosures = syndEntry.enclosures.map { e -> EnclosureDto(url = e.url, type = e.type, length = e.length) },
//        modules = syndEntry.modules,
        date_published = Optional.ofNullable(syndEntry.publishedDate).orElse(Date()),
        main_image_url = syndEntry.enclosures.find { e -> e.type === "image" }?.url, // toodo mag find image enclosure
      )
    } catch (e: Exception) {
      this.log.error(e.message)
      null
    }
  }

//  private fun prepareRequest(corrId: String, prerender: Boolean, url: String): ListenableFuture<Response> {
//    val builderConfig = Dsl.config()
//      .setConnectTimeout(500)
//      .setConnectionTtl(2000)
//      .setFollowRedirect(true)
//      .setMaxRedirects(5)
//      .build()
//
//    val client = Dsl.asyncHttpClient(builderConfig)
//
//    val request = client.prepareGet(url)
////    if (!prerender) {
////      bypassConsentService.tryBypassConsent(corrId, request, url)
////    }
//    return request.execute()
//  }

}
