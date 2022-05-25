package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.DeepArticleRecovery
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ArticleEndpoint {

  private val log = LoggerFactory.getLogger(ArticleEndpoint::class.simpleName)

  @Autowired
  lateinit var deepArticleRecovery: DeepArticleRecovery

  //  @RateLimiter(name="processService", fallbackMethod = "processFallback")
  @GetMapping("/api/intern/articles/recovery")
  fun recovery(
    @RequestParam("url") url: String,
    @RequestParam("correlationId", required = false) correlationId: String?,
  ): ArticleJsonDto {
    val corrId = handleCorrId(correlationId)
    val articleRecovery = ArticleRecovery.METADATA
    log.info("[$corrId] articles/recovery url=$url articleRecovery=$articleRecovery")
    val article = ArticleJsonDto(
      id = "",
      title = "",
      content_text = "",
      url = url,
      date_published = Date(),
    )
    return this.deepArticleRecovery.recoverArticle(corrId, article, articleRecovery)
  }

  @GetMapping("/api/intern/articles/meta")
  fun meta(
    @RequestParam("url") url: String,
    @RequestParam("correlationId", required = false) correlationId: String?,
  ): Map<String, Any> {
    val corrId = handleCorrId(correlationId)
    log.info("[$corrId] articles/meta url=$url")
    return this.deepArticleRecovery.resolveMetadata(corrId, url)
  }
}
