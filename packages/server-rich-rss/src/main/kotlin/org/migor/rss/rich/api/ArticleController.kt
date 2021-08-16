package org.migor.rss.rich.api

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.service.ArticleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam


data class MoreLikeThis(val article: ArticleJsonDto)
data class QualityScore(val article: ArticleJsonDto)

@Controller("/api/external")
class RecommendController {

  @Autowired
  lateinit var articleService: ArticleService

  @PostMapping("/articles/more-like-this")
  fun moreLikeThis(@RequestParam("token") token: String,
                @RequestParam("contentType") contentType: String,
                @RequestBody article: ArticleJsonDto): MoreLikeThis {
    return articleService.moreLikeThis(article, token)
  }

  @PostMapping("/articles/quality-score")
  fun score(@RequestParam("token") token: String,
            @RequestParam("contentType") contentType: String,
            @RequestBody article: ArticleJsonDto): QualityScore {
    return articleService.score(article, token)
  }

}
