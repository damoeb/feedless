package org.migor.rich.rss.api

import jakarta.servlet.http.HttpServletRequest
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.migor.rich.rss.util.HttpUtil
import org.migor.rich.rss.util.HttpUtil.createCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.context.request.RequestContextHolder

@Controller
@Profile(AppProfiles.database)
class FeedController {

  private val log = LoggerFactory.getLogger(FeedController::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @GetMapping("/feed:{feedId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(
    request: HttpServletRequest,
    @PathVariable("feedId") feedId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET feed/atom id=$feedId page=$page")
    return feedExporter.to(corrId, HttpStatus.OK, "atom", feedService.findByFeedId(feedId, page))
  }

  @GetMapping("/feed:{feedId}", "/feed:{feedId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(
    request: HttpServletRequest,
    @PathVariable("feedId") feedId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET feed/json id=$feedId page=$page")
    return feedExporter.to(newCorrId(), HttpStatus.OK, "json", feedService.findByFeedId(feedId, page))
  }

//  @GetMapping("/feed:{feedId}/ap", "/feed:{feedId}/pub", "/feed:{feedId}/activitypub", produces = ["application/json;charset=UTF-8"])
//  fun activityPubFeed(@PathVariable("feedId") feedId: String,
//               @PathVariable("type", required = false) type: String?,
//               @RequestParam("page", required = false, defaultValue = "0") page: Int): ResponseEntity<String> {
//    return FeedExporter.toJson(activityPubService.toApFeed(feedService.findByFeedId(feedId, page, type)))
//  }

//  @PutMapping("/feed:{feedId}", "/feed:{feedId}/put")
//  fun addToFeed(
//    @RequestParam(ApiParams.corrId, required = false) corrId: String?,
//    @PathVariable("feedId") feedId: String,
//    @RequestParam("opSecret") feedOpSecret: String,
//    @RequestBody article: RichArticle
//  ) {
//    return feedService.addToFeed(handleCorrId(corrId), feedId, article, feedOpSecret)
//  }

//  @DeleteMapping("/feed:{feedId}", "/feed:{feedId}/delete")
//  fun deleteFromFeed(
//    @RequestParam(ApiParams.corrId, required = false) corrId: String?,
//    @PathVariable("feedId") feedId: String,
//    @RequestParam("articleId") articleId: String,
//    @RequestParam("opSecret") feedOpSecret: String
//  ) {
//    return feedService.deleteFromFeed(handleCorrId(corrId), feedId, articleId, feedOpSecret)
//  }

}
