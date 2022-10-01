package org.migor.rich.rss.api

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.exporter.FeedExporter
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.AuthToken
import org.migor.rich.rss.service.AuthTokenType
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.transform.WebToFeedService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletRequest
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@RestController
class WebToFeedEndpoint {

  private val log = LoggerFactory.getLogger(WebToFeedEndpoint::class.simpleName)

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @Autowired
  lateinit var articleRecoveryService: ArticleRecovery

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired(required = false)
  lateinit var feedService: FeedService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var env: Environment

  @Throttled
  @Timed
  @GetMapping("/api/web-to-feed", ApiUrls.webToFeed)
  fun handle(
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
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
    @RequestParam("out", required = false) responseTypeParam: String?,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    meterRegistry.counter("w2f", listOf(Tag.of("version", version))).increment()

    val corrId = handleCorrId(corrIdParam)
    val (responseType, convert) = feedExporter.resolveResponseType(corrId, responseTypeParam)

    log.info("[$corrId] w2f/$responseType url=$url recovery=$articleRecovery filter=$filter")

    val extendedFeedRule = feedDiscoveryService.asExtendedRule(
      corrId, url, linkXPath, dateXPath, contextXPath, extendContext,
      filter, version, articleRecoveryService.resolveArticleRecovery(articleRecovery), prerender, puppeteerScript
    )

    return runCatching {
      val decoded = authService.validateAuthToken(corrId, request)
      convert(
        webToFeedService.applyRule(corrId, extendedFeedRule, decoded),
        HttpStatus.OK,
        1.toDuration(DurationUnit.HOURS)
      )
    }
      .getOrElse {
        if (it is HostOverloadingException) {
          throw it
        }
        log.error("[${corrId}] ${it.message}")
        val article = webToFeedService.createMaintenanceArticle(it, url)
        convert(
          webToFeedService.createMaintenanceFeed(corrId, url, extendedFeedRule.feedUrl, article),
          HttpStatus.SERVICE_UNAVAILABLE,
          1.toDuration(DurationUnit.DAYS)
        )
      }
  }

  @Throttled
  @Timed
  @GetMapping("/api/web-to-feed/persist")
  fun persist(
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
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
    @RequestParam("out", required = false) responseTypeParam: String?,
    request: HttpServletRequest
  ): ResponseEntity<String> {

    return if (env.acceptsProfiles(Profiles.of("!database"))) {
      ResponseEntity.badRequest().body("not supported")
    } else {
      meterRegistry.counter("w2f", listOf(Tag.of("version", "legacy"))).increment()

      val corrId = handleCorrId(corrIdParam)

      log.info("[$corrId] persist w2f feedUrl=${url}")

      val extendedFeedRule = feedDiscoveryService.asExtendedRule(
        corrId, url, linkXPath, dateXPath, contextXPath, extendContext,
        filter, version, articleRecoveryService.resolveArticleRecovery(articleRecovery), prerender, puppeteerScript
      )

      return feedService.persist(corrId, extendedFeedRule)
    }


  }

  // http://localhost:4200/api/feed?url=http%3A%2F%2Fheise.de&pContext=%2F%2Fbody%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D&x=s
  @GetMapping("/api/feed")
  fun legacyFeed(
    @RequestParam("url") url: String,
    @RequestParam("pContext") contextXPath: String,
    @RequestParam("pLink") linkXPath: String,
    @RequestParam("responseType") responseTypeParam: String,
    req: HttpServletRequest
  ): ResponseEntity<String> {
    return if (env.acceptsProfiles(Profiles.of("!legacy"))) {
      ResponseEntity.badRequest().body("not supported")
    } else {
      meterRegistry.counter("w2f", listOf(Tag.of("version", "legacy"))).increment()

      val corrId = newCorrId()

      val (_, convert) = feedExporter.resolveResponseType(corrId, responseTypeParam)
      log.info("[$corrId] legacy feed feedUrl=${req.pathInfo}")
      val version = propertyService.webToFeedVersion

      val extendedFeedRule = feedDiscoveryService.asExtendedRule(
        corrId, url, linkXPath, null, contextXPath, "",
        null, version, ArticleRecoveryType.NONE, false, null
      )

      runCatching {
        val token = AuthToken(
          type = AuthTokenType.LEGACY,
          isAnonymous = true,
          isWeb = false,
          issuedAt = Date(),
          remoteAddr = null
        )
        convert(
          webToFeedService.applyRule(corrId, extendedFeedRule, token),
          HttpStatus.OK,
          1.toDuration(DurationUnit.HOURS)
        )
      }
        .getOrElse {
          if (it is HostOverloadingException) {
            throw it
          }
          val article = webToFeedService.createMaintenanceArticle(url)
          val feed = webToFeedService.createMaintenanceFeed(corrId, url, url, article)
          return feedExporter.to(corrId, HttpStatus.SERVICE_UNAVAILABLE, "atom", feed)
        }

    }
  }

}