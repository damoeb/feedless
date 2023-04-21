package org.migor.rich.rss.api

import jakarta.servlet.http.HttpServletRequest
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.service.BucketService
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

@Controller
@Profile(AppProfiles.database)
class BucketController {

  private val log = LoggerFactory.getLogger(BucketController::class.simpleName)

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @GetMapping("/bucket:{bucketId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(
    request: HttpServletRequest,
    @PathVariable("bucketId") bucketId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET bucket/atom id=$bucketId page=$page")
    return feedExporter.to(corrId, HttpStatus.OK, "atom", bucketService.findFeedByBucketId(bucketId, page))
  }

  @GetMapping("/bucket:{bucketId}/json", "/bucket:{bucketId}", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(
    request: HttpServletRequest,
    @PathVariable("bucketId") bucketId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET bucket/json id=$bucketId page=$page")
    return feedExporter.to(corrId, HttpStatus.OK, "json", bucketService.findFeedByBucketId(bucketId, page))
  }

//  @PutMapping("/bucket:{bucketId}/put")
//  fun addToBucket(
//    @RequestParam( ApiParams.corrId, required = false) corrId: String?,
//    @PathVariable("bucketId") bucketId: String,
//    @RequestParam("opSecret") feedsOpSecret: String,
//    @RequestBody article: RichArticle
//  ) {
//    return bucketService.addToBucket(handleCorrId(corrId), bucketId, article, feedsOpSecret)
//  }

//  @DeleteMapping("/bucket:{bucketId}/delete")
//  fun deleteFromBucket(
//    @RequestParam( ApiParams.corrId, required = false) corrId: String?,
//    @PathVariable("bucketId") bucketId: String,
//    @RequestParam("articleId") articleId: String,
//    @RequestParam("opSecret") feedsOpSecret: String
//  ) {
//    return bucketService.deleteFromBucket(handleCorrId(corrId), bucketId, articleId, feedsOpSecret)
//  }
}
