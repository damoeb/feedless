package org.migor.rss.rich.api

import org.migor.rss.rich.service.ExporterTargetService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.FeedExporter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class FeedController {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @GetMapping("/feed:{feedId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("feedId") feedId: String): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findByFeedId(feedId))
  }

  @GetMapping("/feed:{feedId}", "/feed:{feedId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("feedId") feedId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findByFeedId(feedId))
  }

  @GetMapping("/feed:{feedId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("feedId") feedId: String): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findByFeedId(feedId))
  }
}
