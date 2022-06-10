package org.migor.rich.rss.api

import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.transform.WebToFeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@RestController
class WebToFeedEndpoint {

  private val log = LoggerFactory.getLogger(WebToFeedEndpoint::class.simpleName)

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @Autowired
  lateinit var articleRecovery: ArticleRecovery

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @Throttled
  @GetMapping("/api/web-to-feed", "/api/w2f")
  fun handle(
    @RequestParam("corrId", required = false) corrIdParam: String?,
    @RequestParam("url") url: String,
    @RequestParam("link") linkXPath: String,
    @RequestParam("x", defaultValue = "") extendContext: String,
    @RequestParam("context") contextXPath: String,
    @RequestParam("date", required = false) dateXPath: String?,
    @RequestParam("re", required = false) articleRecovery: String?,
    @RequestParam("pp", required = false, defaultValue = "false") prerender: Boolean,
    @RequestParam("ppS", required = false) puppeteerScript: String?,
    @RequestParam("q") filter: String?,
    @RequestParam("v") version: String,
    @RequestParam("token") token: String,
    @RequestParam("out", required = false) responseTypeParam: String?,
    httpServletRequest: HttpServletRequest
  ): ResponseEntity<String> {
    val corrId = handleCorrId(corrIdParam)
    val (responseType, convert) = feedExporter.resolveResponseType(corrId, responseTypeParam)

    log.info("[$corrId] w2f/$responseType url=$url recovery=$articleRecovery filter=$filter")

    val extendedFeedRule = webToFeedService.asExtendedRule(
      corrId, url, linkXPath, dateXPath, contextXPath, extendContext,
      filter, version, this.articleRecovery.resolveArticleRecovery(articleRecovery), prerender, puppeteerScript
    )

    return runCatching {
      val decoded = authService.validateAuthToken(corrId, token)
      convert(
        webToFeedService.applyRule(corrId, extendedFeedRule, decoded),
        1.toDuration(DurationUnit.HOURS)
      )
    }
      .getOrElse {
        convert(
          webToFeedService.createMaintenanceFeed(corrId, it, url, extendedFeedRule.feedUrl),
          1.toDuration(DurationUnit.DAYS)
        )
      }
  }
}
