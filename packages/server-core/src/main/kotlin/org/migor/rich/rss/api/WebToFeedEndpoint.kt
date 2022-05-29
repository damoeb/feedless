package org.migor.rich.rss.api

import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.transform.WebToFeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.FeedExporter
import org.migor.rich.rss.util.FeedUtil.resolveArticleRecovery
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
  lateinit var authService: AuthService

  @Throttled
  @GetMapping("/api/web-to-feed", "/api/w2f", "/api/web-to-feed/{responseType}")
  fun handle(
    @RequestParam("corrId", required = false) corrIdParam: String?,
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("dateXPath", required = false) dateXPath: String?,
    @RequestParam("recovery", required = false) articleRecovery: String?,
    @RequestParam("filter") filter: String?,
    @RequestParam("version") version: String,
    @RequestParam("token", required = false) token: String?,
    @PathVariable("responseType", required = false) responseTypeParam: String?,
    httpServletRequest: HttpServletRequest
  ): ResponseEntity<String> {
    val corrId = handleCorrId(corrIdParam)
    val (responseType, convert) = FeedExporter.resolveResponseType(corrId, responseTypeParam)

    log.info("[$corrId] w2f/$responseType url=$url recovery=$articleRecovery filter=$filter")

    val extendedFeedRule = webToFeedService.asExtendedRule(
      corrId, url, linkXPath, dateXPath, contextXPath, extendContext,
      filter, version, resolveArticleRecovery(articleRecovery)
    )

    return runCatching {
      authService.validateAuthToken(corrId, token)
      convert(
        webToFeedService.applyRule(corrId, extendedFeedRule),
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
