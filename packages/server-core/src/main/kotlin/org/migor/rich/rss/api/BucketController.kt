package org.migor.rich.rss.api

import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile("database2")
class BucketController {

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @GetMapping("/bucket:{bucketId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(
    @PathVariable("bucketId") bucketId: String,
    @PathVariable("type", required = false) type: String?,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    return feedExporter.to(newCorrId(), HttpStatus.OK, "rss", bucketService.findByBucketId(bucketId, page, type))
  }

  @GetMapping("/bucket:{bucketId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(
    @PathVariable("bucketId") bucketId: String,
    @PathVariable("type", required = false) type: String?,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    return feedExporter.to(newCorrId(), HttpStatus.OK, "atom", bucketService.findByBucketId(bucketId, page, type))
  }

  @GetMapping("/bucket:{bucketId}/json", "/bucket:{bucketId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(
    @PathVariable("bucketId") bucketId: String,
    @PathVariable("type", required = false) type: String?,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    return feedExporter.to(newCorrId(), HttpStatus.OK, "json", bucketService.findByBucketId(bucketId, page, type))
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
