package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ArticleEndpoint {

  private val log = LoggerFactory.getLogger(ArticleEndpoint::class.simpleName)

  @Autowired
  lateinit var articleRecovery: ArticleRecovery

  //  @RateLimiter(name="processService", fallbackMethod = "processFallback")
  @GetMapping("/api/intern/articles/recovery")
  fun recovery(
    @RequestParam("url") url: String,
    @RequestParam("corrId", required = false) corrIdParam: String?,
  ): ArticleJsonDto? {
    val corrId = handleCorrId(corrIdParam)
    val articleRecovery = ArticleRecoveryType.METADATA
    log.info("[$corrId] articles/recovery url=$url articleRecovery=$articleRecovery")
    return this.articleRecovery.recoverArticle(corrId, url, articleRecovery)
  }

  @GetMapping("/api/intern/articles/meta")
  fun meta(
    @RequestParam("url") url: String,
    @RequestParam("corrId", required = false) corrIdParam: String?,
  ): Map<String, Any> {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] articles/meta url=$url")
    return this.articleRecovery.resolveMetadata(corrId, url)
  }
}
