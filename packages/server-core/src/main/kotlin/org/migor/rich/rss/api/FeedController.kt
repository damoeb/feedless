package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile("database2")
class FeedController {

    @Autowired
    lateinit var feedService: FeedService

    @Autowired
    lateinit var feedExporter: FeedExporter

    @GetMapping("/feed:{feedId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
    fun atomFeed(
        @PathVariable("feedId") feedId: String,
        @PathVariable("type", required = false) type: String?,
        @RequestParam("page", required = false, defaultValue = "0") page: Int
    ): ResponseEntity<String> {
        return feedExporter.to(newCorrId(), HttpStatus.OK, "atom", feedService.findByFeedId(feedId, page, type))
    }

    @GetMapping("/feed:{feedId}", "/feed:{feedId}/json", produces = ["application/json;charset=UTF-8"])
    fun jsonFeed(
        @PathVariable("feedId") feedId: String,
        @PathVariable("type", required = false) type: String?,
        @RequestParam("page", required = false, defaultValue = "0") page: Int
    ): ResponseEntity<String> {
        return feedExporter.to(newCorrId(), HttpStatus.OK, "json", feedService.findByFeedId(feedId, page, type))
    }

//  @GetMapping("/feed:{feedId}/ap", "/feed:{feedId}/pub", "/feed:{feedId}/activitypub", produces = ["application/json;charset=UTF-8"])
//  fun activityPubFeed(@PathVariable("feedId") feedId: String,
//               @PathVariable("type", required = false) type: String?,
//               @RequestParam("page", required = false, defaultValue = "0") page: Int): ResponseEntity<String> {
//    return FeedExporter.toJson(activityPubService.toApFeed(feedService.findByFeedId(feedId, page, type)))
//  }

    @PutMapping("/feed:{feedId}", "/feed:{feedId}/put")
    fun addToFeed(
        @RequestParam( ApiParams.corrId, required = false) corrId: String?,
        @PathVariable("feedId") feedId: String,
        @RequestParam("opSecret") feedOpSecret: String,
        @RequestBody article: RichArticle
    ) {
        return feedService.addToFeed(handleCorrId(corrId), feedId, article, feedOpSecret)
    }

    @DeleteMapping("/feed:{feedId}", "/feed:{feedId}/delete")
    fun deleteFromFeed(
        @RequestParam( ApiParams.corrId, required = false) corrId: String?,
        @PathVariable("feedId") feedId: String,
        @RequestParam("articleId") articleId: String,
        @RequestParam("opSecret") feedOpSecret: String
    ) {
        return feedService.deleteFromFeed(handleCorrId(corrId), feedId, articleId, feedOpSecret)
    }

}
