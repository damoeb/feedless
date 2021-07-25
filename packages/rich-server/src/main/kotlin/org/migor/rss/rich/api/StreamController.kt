package org.migor.rss.rich.api

import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.FeedExporter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@Controller
class StreamController {

  @Autowired
  lateinit var feedService: FeedService

  @GetMapping("/stream:{streamId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("streamId") streamId: String): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findByStreamId(streamId))
  }

  @GetMapping("/stream:{streamId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("streamId") streamId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findByStreamId(streamId))
  }

  @GetMapping("/stream:{streamId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("streamId") streamId: String): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findByStreamId(streamId))
  }

}
