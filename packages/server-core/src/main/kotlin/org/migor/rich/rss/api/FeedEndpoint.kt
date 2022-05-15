package org.migor.rich.rss.api

import com.rometools.rome.feed.synd.SyndEntry
import org.asynchttpclient.Dsl
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import org.jsoup.Jsoup
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.FeedDiscovery
import org.migor.rich.rss.api.dto.FeedDiscoveryOptions
import org.migor.rich.rss.api.dto.FeedDiscoveryResults
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.discovery.FeedReference
import org.migor.rich.rss.discovery.GenericFeedLocator
import org.migor.rich.rss.discovery.NativeFeedLocator
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.harvest.feedparser.FeedType
import org.migor.rich.rss.service.BypassConsentService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.transform.GenericFeedRule
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.FeedExporter
import org.migor.rich.rss.util.FeedUtil
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@RestController
class FeedEndpoint {

  private val log = LoggerFactory.getLogger(FeedEndpoint::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bypassConsentService: BypassConsentService

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var genericFeedLocator: GenericFeedLocator

  @GetMapping("/api/feeds/discover")
  fun discoverFeeds(
    @RequestParam("homepageUrl") homepageUrl: String,
    @RequestParam("correlationId", required = false) correlationId: String?,
    @RequestParam(name = "prerender", defaultValue = "false") prerenderParam: Boolean
  ): FeedDiscovery {
    val corrId = handleCorrId(correlationId)
    val prerender = resolvePrerender(prerenderParam)
    fun buildDiscoveryResponse(
      url: String,
      mimeType: MimeType?,
      nativeFeeds: List<FeedReference>,
      relatedFeeds: List<Feed>,
      genericFeedRules: List<GenericFeedRule> = emptyList(),
      body: String = "",
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
          nativeFeeds = nativeFeeds,
          relatedFeeds = relatedFeeds,
          genericFeedRules = genericFeedRules,
          body = body,
          failed = failed,
          errorMessage = errorMessage
        )
      )
    }
    log.info("[$corrId] Discover feeds in url=$homepageUrl, prerender=$prerender")
    return try {
      val parsedUrl = parseUrl(homepageUrl)
      val url = if (prerender) {
        "${propertyService.puppeteerHost}/prerender/?url=${
          URLEncoder.encode(
            parsedUrl,
            StandardCharsets.UTF_8
          )
        }&correlationId=${corrId}"
      } else {
        parsedUrl
      }

      val request = prepareRequest(corrId, prerender, url)
      log.info("[$corrId] GET $url")
      val response = request.get()
      log.info("[$corrId] -> ${response.statusCode}")

      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(response)

      val relatedFeeds = feedService.findRelatedByUrl(parsedUrl)
      if (feedType !== FeedType.NONE) {
        val feed = feedService.parseFeed(corrId, HarvestResponse(url, response))
        log.info("[$corrId] is native-feed")
        buildDiscoveryResponse(
          url,
          mimeType,
          relatedFeeds = relatedFeeds,
          nativeFeeds = listOf(FeedReference(url = url, type = feedType, title = feed.feed.title))
        )
      } else {
        val document = Jsoup.parse(response.responseBody)
        document.select("script,.hidden,style").remove()
        val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
        val genericFeedRules = genericFeedLocator.locateInDocument(corrId, document, url)
        log.info("[$corrId] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size} relatedFeeds=${relatedFeeds.size}")
        buildDiscoveryResponse(url, mimeType, nativeFeeds, relatedFeeds, genericFeedRules, document.html())
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

  private fun resolvePrerender(prerender: Boolean): Boolean {
    return if(environment.acceptsProfiles(Profiles.of("proxy"))) {
      false
    } else {
      prerender
    }
  }

  @GetMapping("/api/feeds/transform")
  fun transformFeed(
    @RequestParam("feedUrl") feedUrl: String,
    @RequestParam("correlationId", required = false) correlationId: String?,
    @RequestHeader("authorization", required = false) authHeader: String?,
    @RequestParam("targetFormat", required = false, defaultValue = "json") targetFormat: String
  ): ResponseEntity<String> {
    val corrId = handleCorrId(correlationId)
    try {
      val syndFeed = this.feedService.parseFeedFromUrl(corrId, feedUrl, authHeader).feed
      val feed = FeedJsonDto(
        id = syndFeed.link,
        name = syndFeed.title,
        description = syndFeed.description,
        home_page_url = syndFeed.link,
        date_published = syndFeed.publishedDate,
        items = syndFeed.entries.filterNotNull().mapNotNull { syndEntry -> this.toArticle(syndEntry) },
        feed_url = syndFeed.link,
        expired = false,
        tags = syndFeed.categories.map { category -> category.name },
      )
      return when (targetFormat.lowercase()) {
        "atom" -> FeedExporter.toAtom(feed)
        "rss" -> FeedExporter.toRss(feed)
        "json" -> FeedExporter.toJson(feed)
        else -> throw ApiException(
          ApiErrorCode.UNKNOWN_FEED_FORMAT,
          "Requested targetFormat '$targetFormat' is not supported. Available: [atom, rss, json]"
        )
      }
    } catch (e: ApiException) {
      return badJsonResponse(e)
    } catch (e: Exception) {
      log.error("[$corrId] Cannot parse feed $feedUrl", e)
      return badJsonResponse(ApiException(ApiErrorCode.INTERNAL_ERROR, e.message))
    }
  }

  private fun badJsonResponse(e: ApiException): ResponseEntity<String> {
    return ResponseEntity.badRequest()
      .header("Content-Type", "application/json")
      .body(e.toJson())
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
        enclosures = JsonUtil.gson.toJson(syndEntry.enclosures),
        date_published = Optional.ofNullable(syndEntry.publishedDate).orElse(Date()),
        commentsFeedUrl = null,
        main_image_url = null, // toodo mag find image enclosure
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun prepareRequest(corrId: String, prerender: Boolean, url: String): ListenableFuture<Response> {
    val builderConfig = Dsl.config()
      .setConnectTimeout(500)
      .setConnectionTtl(2000)
      .setFollowRedirect(true)
      .setMaxRedirects(5)
      .build()

    val client = Dsl.asyncHttpClient(builderConfig)

    val request = client.prepareGet(url)
    if (!prerender) {
      bypassConsentService.tryBypassConsent(corrId, request, url)
    }
    return request.execute()
  }

  private fun parseUrl(urlParam: String): String {
    return if (urlParam.startsWith("https://") || urlParam.startsWith("http://")) {
      URL(urlParam)
      urlParam
    } else {
      parseUrl("https://$urlParam")
    }
  }
}
