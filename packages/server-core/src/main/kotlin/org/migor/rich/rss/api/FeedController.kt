package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.service.ExporterTargetService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.FeedExporter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile("rich")
class FeedController {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @GetMapping("/feed:{feedId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(
    @PathVariable("feedId") feedId: String,
    @PathVariable("type", required = false) type: String?,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    return FeedExporter.toRss(feedService.findByFeedId(feedId, page, type))
  }

  @GetMapping("/feed:{feedId}", "/feed:{feedId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(
    @PathVariable("feedId") feedId: String,
    @PathVariable("type", required = false) type: String?,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    return FeedExporter.toAtom(feedService.findByFeedId(feedId, page, type))
  }

  @GetMapping("/feed:{feedId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(
    @PathVariable("feedId") feedId: String,
    @PathVariable("type", required = false) type: String?,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    return FeedExporter.toJson(feedService.findByFeedId(feedId, page, type))
  }

//  @GetMapping("/feed:{feedId}/ap", "/feed:{feedId}/pub", "/feed:{feedId}/activitypub", produces = ["application/json;charset=UTF-8"])
//  fun activityPubFeed(@PathVariable("feedId") feedId: String,
//               @PathVariable("type", required = false) type: String?,
//               @RequestParam("page", required = false, defaultValue = "0") page: Int): ResponseEntity<String> {
//    return FeedExporter.toJson(activityPubService.toApFeed(feedService.findByFeedId(feedId, page, type)))
//  }

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
