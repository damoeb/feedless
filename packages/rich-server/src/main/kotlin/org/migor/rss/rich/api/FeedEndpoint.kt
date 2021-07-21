package org.migor.rss.rich.api

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.api.dto.FeedDiscovery
import org.migor.rss.rich.discovery.FeedLocator
import org.migor.rss.rich.api.dto.FeedDto
import org.migor.rss.rich.api.dto.SourceEntryDto
import org.migor.rss.rich.service.FeedService
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
    val feed = FeedDto(name = syndFeed.title,
      id= "",
      description= syndFeed.description,
      pubDate= syndFeed.publishedDate,
      entries = syndFeed.entries.map { syndEntry: SyndEntry? -> this.toEntry(syndEntry!!) },
      link= syndFeed.link,
    )
    return FeedExporter.toJson(feed)
  }

  private fun toEntry(syndEntry: SyndEntry): SourceEntryDto {
    val entryDto = SourceEntryDto()
    entryDto["id"] = syndEntry.uri
    entryDto["title"] = syndEntry.title!!
    entryDto["categories"] = syndEntry.categories
    entryDto["description"] = syndEntry.description
    entryDto["contents"] = syndEntry.contents
    entryDto["link"] = syndEntry.link
    entryDto["author"] = syndEntry.author
    entryDto["enclosures"] = syndEntry.enclosures
    entryDto["pubDate"] = syndEntry.publishedDate
    return entryDto
  }
}
