package org.migor.rss.rich.api

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.service.ExporterTargetService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.FeedExporter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller
class StreamController {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @GetMapping("/stream:{streamId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("streamId") streamId: String): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findByStreamId(streamId))
  }

  @GetMapping("/stream:{streamId}", "/stream:{streamId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("streamId") streamId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findByStreamId(streamId))
  }

  @GetMapping("/stream:{streamId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("streamId") streamId: String): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findByStreamId(streamId))
  }

  @PutMapping("/stream:{streamId}/put")
  fun addToFeed(
    @PathVariable("streamId") streamId: String,
    @RequestParam("token") token: String,
    @RequestBody article: ArticleJsonDto
  ) {
    return exporterTargetService.addToStream(streamId, article, token)
  }

  @DeleteMapping("/stream:{streamId}/delete")
  fun deleteFromFeed(
    @PathVariable("streamId") streamId: String,
    @RequestParam("article") articleId: String,
    @RequestParam("token") token: String
  ) {
    return exporterTargetService.deleteFromtream(streamId, articleId, token)
  }
}
