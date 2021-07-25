package org.migor.rss.rich.api

import com.rometools.rome.feed.synd.SyndEntry
import org.asynchttpclient.Dsl
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.discovery.FeedReference
import org.migor.rss.rich.discovery.NativeFeedLocator
import org.migor.rss.rich.harvest.feedparser.GeneratedFeedLocator
import org.migor.rss.rich.harvest.feedparser.NullFeedParser
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class FeedEndpoint {

  private val log = LoggerFactory.getLogger(FeedEndpoint::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @GetMapping("/api/feeds/discover")
  fun discoverFeeds(@RequestParam("url") url: String): List<FeedReference> {
    return try {
      val builderConfig = Dsl.config()
        .setConnectTimeout(500)
        .setConnectionTtl(2000)
        .setFollowRedirect(true)
        .setMaxRedirects(5)
        .build()

      val client = Dsl.asyncHttpClient(builderConfig)
      // todo mag check if is url
      val request = client.prepareGet(url).execute()
      val response = request.get()

      val nativeFeeds = NativeFeedLocator.locate(response, url)
      val generatedFeed = GeneratedFeedLocator.locate(response, url)

      if (generatedFeed.isPresent) {
        nativeFeeds.plus(generatedFeed.get())
      } else {
        nativeFeeds
      }
    } catch (e: Exception) {
      log.error("Unable to discover feeds", e)
      emptyList()
    }
  }

  @GetMapping("/api/feeds/parse")
  fun parseFeed(@RequestParam("url") url: String): ResponseEntity<String> {
    try {
      val syndFeed = this.feedService.parseFeed(url).feed
      val feed = FeedJsonDto(
        id = syndFeed.link,
        name = syndFeed.title,
        home_page_url = syndFeed.uri,
        description = syndFeed.description,
        expired = false,
        date_published = syndFeed.publishedDate,
        items = syndFeed.entries.map { syndEntry: SyndEntry? -> this.toArticle(syndEntry!!) },
        feed_url = syndFeed.link,
      )
      return FeedExporter.toJson(feed)
    } catch(e: Exception) {
      log.error("Cannot parse feed $url", e);
      return ResponseEntity.badRequest()
        .body(e.message)
    }
  }

  private fun toArticle(syndEntry: SyndEntry): ArticleJsonDto {
    return ArticleJsonDto(
      id = syndEntry.uri,
      title = syndEntry.title!!,
      tags = syndEntry.categories.map { syndCategory -> syndCategory.name }.joinToString { ", " },
      content_text = syndEntry.description.value,
      content_html = syndEntry.contents.filter { syndContent -> syndContent.type.contains("html") }.map { syndContent -> syndContent.value }.firstOrNull(),
      url = syndEntry.link,
      author = syndEntry.author,
      enclosures = JsonUtil.gson.toJson(syndEntry.enclosures),
      date_published = syndEntry.publishedDate,
      commentsFeedUrl = null,
    )
  }
}
