package org.migor.rss.rich.api

import org.migor.rss.rich.util.FeedExporter
import org.migor.rss.rich.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class SourceController {

  @Autowired
  lateinit var feedService: FeedService

  @GetMapping("/source:{sourceId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("sourceId") sourceId: String): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findBySourceId(sourceId))
  }

  @GetMapping("/source:{sourceId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("sourceId") sourceId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findBySourceId(sourceId))
  }

  @GetMapping("/source:{sourceId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("sourceId") sourceId: String): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findBySourceId(sourceId))
  }

}
