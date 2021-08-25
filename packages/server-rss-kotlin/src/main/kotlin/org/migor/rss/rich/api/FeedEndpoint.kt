package org.migor.rss.rich.api

import com.rometools.rome.feed.synd.SyndEntry
import org.asynchttpclient.Dsl
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedDiscovery
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.discovery.FeedReference
import org.migor.rss.rich.discovery.NativeFeedLocator
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.feedparser.FeedType
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URL
import java.util.*


@RestController
class FeedEndpoint {

  private val log = LoggerFactory.getLogger(FeedEndpoint::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var nativeFeedLocator: NativeFeedLocator

  @GetMapping("/api/feeds/discover")
  fun discoverFeeds(@RequestParam("url") urlParam: String): FeedDiscovery {
    fun buildResponse(feeds: List<FeedReference>, body: String = ""): FeedDiscovery {
      return FeedDiscovery(feeds, body)
    }
    log.info("Discover feeds in url=$urlParam")
    return try {
      val builderConfig = Dsl.config()
        .setConnectTimeout(500)
        .setConnectionTtl(2000)
        .setFollowRedirect(true)
        .setMaxRedirects(5)
        .build()

      val client = Dsl.asyncHttpClient(builderConfig)
      val url = parseUrl(urlParam)
      val request = client.prepareGet(url).execute()
      val response = request.get()

      val feedType = FeedUtil.detectFeedTypeForResponse(response)

      if (feedType !== FeedType.NONE) {
        val feed = feedService.parseFeed(HarvestResponse(url, response))
        log.info("Detected native-feed url=$urlParam")
        buildResponse(listOf(FeedReference(url = url, type = feedType, title = feed.feed.title)))
      } else {
        val nativeFeeds = nativeFeedLocator.locateInDocument(response, url)
        log.info("Found ${nativeFeeds.size} native-feeds in url=$urlParam")
        buildResponse(nativeFeeds, response.responseBody)
      }
    } catch (e: Exception) {
      log.error("Unable to discover feeds", e.message)
      buildResponse(emptyList())
    }
  }

  private fun parseUrl(urlParam: String): String {
    return if (urlParam.startsWith("https://") || urlParam.startsWith("http://")) {
      URL(urlParam)
      urlParam
    } else {
      parseUrl("https://${urlParam}")
    }
  }

  @GetMapping("/api/feeds/parse")
  fun parseFeed(@RequestParam("url") url: String): ResponseEntity<String> {
    try {
      val syndFeed = this.feedService.parseFeedFromUrl(url).feed
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
      log.error("Cannot parse feed $url", e);
      return ResponseEntity.badRequest()
        .header("Content-Type", "application/json")
        .body(e.message)
    }
  }

  @GetMapping("/api/feeds/query")
  fun feedFromQueryEngines(@RequestParam("q") query: String, @RequestParam("token") token: String): ResponseEntity<String> {
    try {
      feedService.queryViaEngines(query, token)
      return ResponseEntity.ok("")
    } catch (e: Exception) {
      log.error("Failed feedFromQueryEngines $query", e);
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
        content_html = syndEntry.contents.filter { syndContent -> syndContent.type.contains("html") }
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
