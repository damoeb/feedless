package org.migor.rss.rich.api

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedDiscovery
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.discovery.FeedLocator
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.util.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class FeedEndpoint {

  @Autowired
  lateinit var feedService: FeedService

  @GetMapping("/api/feeds/discover")
  fun discoverFeeds(@RequestParam("url") url: String): FeedDiscovery? {
    return FeedDiscovery(feeds = FeedLocator.locate(url))
  }

  @GetMapping("/api/feeds/parse")
  fun parseFeed(@RequestParam("url") url: String): ResponseEntity<String> {
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
  }

  private fun toArticle(syndEntry: SyndEntry): ArticleJsonDto {
    return ArticleJsonDto(
      id = syndEntry.uri,
      title = syndEntry.title!!,
      tags = syndEntry.categories.map { syndCategory -> syndCategory.name }.joinToString { ", " },
      content_text = syndEntry.description.value,
      content_html = syndEntry.contents.filter { syndContent -> syndContent.type.contains("html") }.map { syndContent -> syndContent.value }.first(),
      url = syndEntry.link,
      author = syndEntry.author,
      enclosures = JsonUtil.gson.toJson(syndEntry.enclosures),
      date_published = syndEntry.publishedDate,
      commentsFeedUrl = null,
    )
  }
}
