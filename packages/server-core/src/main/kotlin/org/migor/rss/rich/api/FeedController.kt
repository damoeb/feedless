package org.migor.rss.rich.api

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.service.ExporterTargetService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.util.CryptUtil.handleCorrId
import org.migor.rss.rich.util.FeedExporter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller
class FeedController {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @GetMapping("/feed:{feedId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("feedId") feedId: String,
              @RequestParam("page", required = false, defaultValue = "0") page: Int): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findByFeedId(feedId, page))
  }

  @GetMapping("/feed:{feedId}", "/feed:{feedId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("feedId") feedId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findByFeedId(feedId))
  }

  @GetMapping("/feed:{feedId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("feedId") feedId: String,
               @RequestParam("page", required = false, defaultValue = "0") page: Int): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findByFeedId(feedId))
  }

  @PostMapping("/feed:{feedId}", "/feed:{feedId}/append")
  fun appendToFeed(@PathVariable("feedId") feedId: String,
                   @RequestParam("page", required = false, defaultValue = "0") page: Int): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findByFeedId(feedId, page))
  }


  @PutMapping("/feed:{feedId}", "/feed:{feedId}/put")
  fun addToFeed(
    @RequestParam("correlationId", required = false) correlationId: String?,
    @PathVariable("feedId") feedId: String,
    @RequestParam("opSecret") feedOpSecret: String,
    @RequestBody article: ArticleJsonDto
  ) {
    return feedService.addToFeed(handleCorrId(correlationId), feedId, article, feedOpSecret)
  }

  @DeleteMapping("/feed:{feedId}", "/feed:{feedId}/delete")
  fun deleteFromFeed(
    @RequestParam("correlationId", required = false) correlationId: String?,
    @PathVariable("feedId") feedId: String,
    @RequestParam("articleId") articleId: String,
    @RequestParam("opSecret") feedOpSecret: String
  ) {
    return feedService.deleteFromFeed(handleCorrId(correlationId), feedId, articleId, feedOpSecret)
  }

}
