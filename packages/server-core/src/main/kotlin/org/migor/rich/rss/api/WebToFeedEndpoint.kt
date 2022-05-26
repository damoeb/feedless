package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.FeedJsonDto
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
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@RestController
class WebToFeedEndpoint {

  private val log = LoggerFactory.getLogger(WebToFeedEndpoint::class.simpleName)

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @GetMapping("/api/web-to-feed", "/api/w2f", "/api/web-to-feed/{responseType}")
  fun handle(
    @RequestParam("correlationId", required = false) corrIdParam: String?,
    @RequestParam("url") url: String,
    @RequestParam("linkXPath") linkXPath: String,
    @RequestParam("extendContext") extendContext: String,
    @RequestParam("contextXPath") contextXPath: String,
    @RequestParam("dateXPath", required = false) dateXPath: String?,
    @RequestParam("recovery", required = false) articleRecovery: String?,
    @RequestParam("filter") filter: String?,
    @RequestParam("version") version: String,
    @PathVariable("responseType", required = false) responseTypeParam: String?
  ): ResponseEntity<String> {
    val corrId = handleCorrId(corrIdParam)
    val (responseType, convert) = handleResponseType(corrId, responseTypeParam)

    log.info("[$corrId] w2f/$responseType url=$url recovery=$articleRecovery filter=$filter")

    val extendedFeedRule = webToFeedService.asExtendedRule(
      corrId, url, linkXPath, dateXPath, contextXPath, extendContext,
      filter, version, resolveArticleRecovery(articleRecovery)
    )

    return runCatching {
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

  private fun handleResponseType(
    corrId: String,
    responseType: String?
  ): Pair<String, (FeedJsonDto, Duration) -> ResponseEntity<String>> {
    return when (responseType?.lowercase()) {
      "atom" -> "atom" to { feed, retryAfter -> FeedExporter.toAtom(corrId, feed, retryAfter) }
      "rss" -> "rss" to { feed, retryAfter -> FeedExporter.toRss(corrId, feed, retryAfter) }
      else -> "json" to { feed, retryAfter -> FeedExporter.toJson(corrId, feed, retryAfter) }
    }
  }
}
