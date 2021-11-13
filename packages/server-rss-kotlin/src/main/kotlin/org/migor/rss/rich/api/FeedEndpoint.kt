package org.migor.rss.rich.api

import com.rometools.rome.feed.synd.SyndEntry
import org.asynchttpclient.Dsl
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import org.jsoup.Jsoup
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedDiscovery
import org.migor.rss.rich.api.dto.FeedDiscoveryOptions
import org.migor.rss.rich.api.dto.FeedDiscoveryResults
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.discovery.FeedReference
import org.migor.rss.rich.discovery.GenericFeedLocator
import org.migor.rss.rich.discovery.NativeFeedLocator
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.feedparser.FeedType
import org.migor.rss.rich.parser.GenericFeedRule
import org.migor.rss.rich.service.BypassConsentService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.CryptUtil
import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.GetMapping
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
  lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  lateinit var genericFeedLocator: GenericFeedLocator

  @GetMapping("/api/feeds/discover")
  fun discoverFeeds(
    @RequestParam("url") urlParam: String,
    @RequestParam("correlationId", required=false) correlationId: String?,
    @RequestParam(name = "prerender", defaultValue = "false") prerender: Boolean
  ): FeedDiscovery {
    val cid = Optional.ofNullable(correlationId).orElse(CryptUtil.newCorrId())
    fun buildResponse(
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
          originalUrl = urlParam,
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
    log.info("[$cid] Discover feeds in url=$urlParam, prerender=$prerender")
    val parsedUrl = parseUrl(urlParam)
    return try {
      val url = if (prerender) {
        "http://localhost:3000/prerender/?url=${URLEncoder.encode(parsedUrl, StandardCharsets.UTF_8)}&correlationId=${cid}"
      } else {
        parsedUrl
      }

      val request = prepareRequest(cid, prerender, url)
      log.info("[$cid] GET $url")
      val response = request.get()
      log.info("[$cid] -> ${response.statusCode}")

      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(response)

      val relatedFeeds = feedService.findRelatedByUrl(parsedUrl)
      if (feedType !== FeedType.NONE) {
        val feed = feedService.parseFeed(cid, HarvestResponse(url, response))
        log.info("[$cid] is native-feed")
        buildResponse(url, mimeType, relatedFeeds = relatedFeeds, nativeFeeds = listOf(FeedReference(url = url, type = feedType, title = feed.feed.title)))
      } else {
        val document = Jsoup.parse(response.responseBody)
        document.select("script,.hidden,style").remove()
        val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
        val genericFeedRules = genericFeedLocator.locateInDocument(document, url)
        log.info("[$cid] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size} relatedFeeds=${relatedFeeds.size}")
        buildResponse(url, mimeType, nativeFeeds, relatedFeeds, genericFeedRules, document.html())
      }
    } catch (e: Exception) {
      log.error("[$cid] Unable to discover feeds", e.message)
      buildResponse(url = urlParam, nativeFeeds = emptyList(), relatedFeeds = emptyList(), mimeType = null, failed = true, errorMessage = e.message)
    }
  }

  private fun prepareRequest(cid: String, prerender: Boolean, url: String): ListenableFuture<Response> {
    val builderConfig = Dsl.config()
      .setConnectTimeout(500)
      .setConnectionTtl(2000)
      .setFollowRedirect(true)
      .setMaxRedirects(5)
      .build()

    val client = Dsl.asyncHttpClient(builderConfig)

    val request = client.prepareGet(url)
    if (!prerender) {
      bypassConsentService.tryBypassConsent(cid, request, url)
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

  @GetMapping("/api/feeds/parse")
  fun parseFeed(@RequestParam("url") url: String): ResponseEntity<String> {
    val cid = CryptUtil.newCorrId()
    try {
      val syndFeed = this.feedService.parseFeedFromUrl(cid, url).feed
      val feed = FeedJsonDto(
        id = syndFeed.link,
        name = syndFeed.title,
        home_page_url = syndFeed.uri,
        description = syndFeed.description,
        expired = false,
        date_published = syndFeed.publishedDate,
        items = syndFeed.entries.filterNotNull()
          .map { syndEntry -> this.toArticle(syndEntry) }
          .filterNotNull(),
        feed_url = syndFeed.link,
      )
      return FeedExporter.toJson(feed)
    } catch (e: Exception) {
      log.error("[$cid] Cannot parse feed $url", e)
      return ResponseEntity.badRequest()
        .header("Content-Type", "application/json")
        .body(e.message)
    }
  }

  @GetMapping("/api/feeds/query")
  fun feedFromQueryEngines(
    @RequestParam("q") query: String,
    @RequestParam("token") token: String
  ): ResponseEntity<String> {
    val cid = CryptUtil.newCorrId()
    try {
      feedService.queryViaEngines(query, token)
      return ResponseEntity.ok("")
    } catch (e: Exception) {
      log.error("[$cid] Failed feedFromQueryEngines $query", e)
      return ResponseEntity.badRequest()
        .header("Content-Type", "application/json")
        .body(e.message)
    }
  }

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

      ArticleJsonDto(
        id = syndEntry.uri,
        title = syndEntry.title!!,
        tags = syndEntry.categories.map { syndCategory -> syndCategory.name }.toList(),
        content_text = Optional.ofNullable(text).orElse(""),
        content_raw = syndEntry.contents.filter { syndContent -> syndContent.type.contains("html") }
          .map { syndContent -> syndContent.value }
          .firstOrNull(),
        url = syndEntry.link,
        author = syndEntry.author,
        enclosures = JsonUtil.gson.toJson(syndEntry.enclosures),
        date_published = Optional.ofNullable(syndEntry.publishedDate).orElse(Date()),
        commentsFeedUrl = null,
      )
    } catch (e: Exception) {
      null
    }
  }
}
