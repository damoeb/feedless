package org.migor.rss.rich.api

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.service.BucketService
import org.migor.rss.rich.service.ExporterTargetService
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
class BucketController {

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @GetMapping("/bucket:{bucketId}/rss", produces = ["application/rss+xml;charset=UTF-8"])
  fun rssFeed(@PathVariable("bucketId") bucketId: String): ResponseEntity<String> {
    return FeedExporter.toRss(bucketService.findByBucketId(bucketId))
  }

  @GetMapping("/bucket:{bucketId}", "/bucket:{bucketId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(@PathVariable("bucketId") bucketId: String): ResponseEntity<String> {
    return FeedExporter.toAtom(bucketService.findByBucketId(bucketId))
  }

  @GetMapping("/bucket:{bucketId}/json", produces = ["application/json;charset=UTF-8"])
  fun jsonFeed(@PathVariable("bucketId") bucketId: String): ResponseEntity<String> {
    return FeedExporter.toJson(bucketService.findByBucketId(bucketId))
  }

  @PutMapping("/bucket:{bucketId}/put")
  fun addToFeed(
    @PathVariable("bucketId") bucketId: String,
    @RequestParam("token") token: String,
    @RequestBody article: ArticleJsonDto
  ) {
    return exporterTargetService.addToStream(bucketId, article, token)
  }

  @DeleteMapping("/bucket:{bucketId}/delete")
  fun deleteFromFeed(
    @PathVariable("bucketId") bucketId: String,
    @RequestParam("article") articleId: String,
    @RequestParam("token") token: String
  ) {
    return exporterTargetService.deleteFromtream(bucketId, articleId, token)
  }
}
