package org.migor.feedless.api.http

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.WebToFeedParams
import org.migor.feedless.api.auth.AuthService
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.api.Throttled
import org.migor.feedless.harvest.ArticleRecoveryType
import org.migor.feedless.harvest.HostOverloadingException
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.FetchOptions
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRefineOptions
import org.migor.feedless.web.GenericFeedSelectors
import org.migor.feedless.web.PuppeteerWaitUntil
import org.migor.feedless.web.WebToFeedService
import org.migor.feedless.web.WebToFeedTransformer
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URL
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@RestController
class WebToFeedController {

  private val log = LoggerFactory.getLogger(WebToFeedController::class.simpleName)

  @Autowired
  lateinit var webToFeedService: WebToFeedService

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Throttled
  @Timed
  @GetMapping(ApiUrls.webToFeedFromRule)
  fun handle(
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam(WebToFeedParams.url) url: String,
    @RequestParam(WebToFeedParams.linkPath) linkXPath: String,
    @RequestParam(WebToFeedParams.extendContext, defaultValue = "") extendContext: String,
    @RequestParam(WebToFeedParams.contextPath) contextXPath: String,
    @RequestParam(WebToFeedParams.paginationXPath) paginationXPath: String,
    @RequestParam(WebToFeedParams.datePath, required = false) dateXPath: String?,
    @RequestParam(WebToFeedParams.strictMode, required = false, defaultValue = "false") strictMode: Boolean,
    @RequestParam(WebToFeedParams.eventFeed, required = false, defaultValue = "false") dateIsStartOfEvent: Boolean,
    @RequestParam(WebToFeedParams.prerender, required = false, defaultValue = "false") prerender: Boolean,
    @RequestParam(WebToFeedParams.prerenderWaitUntil, required = false) prerenderWaitUntil: PuppeteerWaitUntil?,
    @RequestParam(WebToFeedParams.prerenderScript, required = false) prerenderScript: String?,
    @RequestParam(WebToFeedParams.version) version: String,
    @RequestParam(WebToFeedParams.format, required = false) responseTypeParam: String?,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    meterRegistry.counter("w2f", listOf(Tag.of("version", version))).increment()

    val corrId = handleCorrId(corrIdParam)
    val (responseType, convert) = feedExporter.resolveResponseType(corrId, responseTypeParam)

    log.info("[$corrId] w2f/$responseType url=$url")

    val selectors = GenericFeedSelectors(
      linkXPath = linkXPath,
      contextXPath = contextXPath,
      paginationXPath = paginationXPath,
      extendContext = parseExtendContext(extendContext),
      dateXPath = dateXPath,
      dateIsStartOfEvent = dateIsStartOfEvent
    )
    val parserOptions = GenericFeedParserOptions(
      strictMode = strictMode,
      version = version,
    )
    val fetchOptions = FetchOptions(
      websiteUrl = url,
      prerender = prerender,
      prerenderWaitUntil = prerenderWaitUntil ?: PuppeteerWaitUntil.load,
      prerenderScript = prerenderScript
    )
    val refineOptions = GenericFeedRefineOptions(
      filter = "",
      recovery = ArticleRecoveryType.NONE
    )

    val feedUrl = webToFeedTransformer.createFeedUrl(URL(url), selectors, parserOptions, fetchOptions, refineOptions)

    return runCatching {
      authService.assertToken(request)
      convert(
        webToFeedService.applyRule(corrId, feedUrl, selectors, fetchOptions, parserOptions, refineOptions),
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
          webToFeedService.createMaintenanceFeed(corrId, url, feedUrl, article),
          HttpStatus.SERVICE_UNAVAILABLE,
          1.toDuration(DurationUnit.DAYS)
        )
      }
  }

  private fun parseExtendContext(extendContext: String): ExtendContext {
    return if (StringUtils.isBlank(extendContext)) {
      ExtendContext.NONE
    } else {
      ExtendContext.values().first { it.value == extendContext }
    }
  }

}
