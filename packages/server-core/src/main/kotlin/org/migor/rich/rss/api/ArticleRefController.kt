package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.exporter.JsonFeedExporter
import org.migor.rich.rss.service.ArticleRefService
import org.migor.rich.rss.service.ExporterTargetService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile("stateful")
class ArticleRefController {

  @Autowired
  lateinit var articleRefService: ArticleRefService

  @Autowired
  lateinit var jsonFeedExporter: JsonFeedExporter

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @GetMapping("/articleref:{articleRefId}")
  fun getArticle(
    @RequestParam("corrId", required = false) corrId: String?,
    @PathVariable("articleRefId") articleRefId: String,
  ): RichArticle {
    return articleRefService.findById(handleCorrId(corrId), articleRefId)
  }

//  @GetMapping("/articleref:{articleRefId}/related")
//  fun relatedArticles(
//    @RequestParam("corrId", required = false) corrId: String?,
//    @PathVariable("articleRefId") articleRefId: String,
//    @PathVariable("type", required = false) type: String?,
//    @RequestParam("page", required = false, defaultValue = "0") page: Int
//  ): ResponseEntity<String> {
//    return jsonFeedExporter.toJson(
//      handleCorrId(corrId),
//      articleRefService.findRelatedArticlesFeed(handleCorrId(corrId), articleRefId, page, type)
//    )
//  }
//
//  @GetMapping("/articleref:{articleRefId}/feeds")
//  fun feedsContainingArticle(
//    @RequestParam("corrId", required = false) corrId: String?,
//    @PathVariable("articleRefId") articleRefId: String,
//    @PathVariable("type", required = false) type: String?,
//    @RequestParam("page", required = false, defaultValue = "0") page: Int
//  ): ResponseEntity<String> {
//    return jsonFeedExporter.toJson(
//      handleCorrId(corrId),
//      articleRefService.findFeedsThatFeatureArticleRef(handleCorrId(corrId), articleRefId, page, type)
//    )
//  }

//  @PostMapping("/articleref:{articleRefId}", "/articleref:{articleRefId}/update")
//  fun updateArticleRef(
//    @RequestParam("corrId", required = false) corrId: String?,
//    @PathVariable("articleRefId") articleRefId: String,
//    @RequestParam("opSecret") feedOpSecret: String,
//    @RequestBody article: RichArticle
//  ) {
//    return articleRefService.updateArticleRef(handleCorrId(corrId), articleRefId, article, feedOpSecret)
//  }
//
//  @DeleteMapping("/articleref:{articleRefId}", "/articleref:{articleRefId}/delete")
//  fun deleteArticleRef(
//    @RequestParam("corrId", required = false) corrId: String?,
//    @PathVariable("articleRefId") articleRefId: String,
//    @RequestParam("opSecret") feedOpSecret: String
//  ) {
//    return articleRefService.deleteArticleRef(handleCorrId(corrId), articleRefId, feedOpSecret)
//  }

}
