package org.migor.feedless.api.http

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.WebToFeedParamsV1
import org.migor.feedless.api.WebToFeedParamsV2
import org.migor.feedless.api.auth.IAuthService
import org.migor.feedless.api.graphql.DtoResolver.toDto
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.generated.types.ScrapeEmitType
import org.migor.feedless.generated.types.ScrapePage
import org.migor.feedless.generated.types.ScrapePrerender
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.harvest.HostOverloadingException
import org.migor.feedless.service.HttpService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.service.getRootElement
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRefineOptions
import org.migor.feedless.web.GenericFeedSelectors
import org.migor.feedless.web.PuppeteerWaitUntil
import org.migor.feedless.web.WebToFeedService
import org.migor.feedless.web.WebToFeedTransformer
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
  lateinit var authService: IAuthService

  @Autowired
  lateinit var feedExporter: FeedExporter

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var scrapeService: ScrapeService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Throttled
  @Timed
  @GetMapping(ApiUrls.webToFeedFromRule, "/api/w2f")
  fun webToFeed(
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam(WebToFeedParamsV2.url, required = false) urlV2: String?,
    @RequestParam(WebToFeedParamsV2.linkPath, required = false) linkXPathV2: String?,
    @RequestParam(WebToFeedParamsV2.extendContext, required = false) extendContextV2: String?,
    @RequestParam(WebToFeedParamsV2.contextPath, required = false) contextXPathV2: String?,
    @RequestParam(WebToFeedParamsV2.debug, required = false, defaultValue = "false") debug: Boolean?,
    @RequestParam(WebToFeedParamsV2.filter, required = false, defaultValue = "") filter: String?,
    @RequestParam(WebToFeedParamsV2.paginationXPath, required = false) paginationXPath: String?,
    @RequestParam(WebToFeedParamsV2.datePath, required = false) dateXPathV2: String?,
    @RequestParam(WebToFeedParamsV2.strictMode, required = false) strictMode: Boolean?,
    @RequestParam(WebToFeedParamsV2.eventFeed, required = false) dateIsStartOfEvent: Boolean?,
    @RequestParam(WebToFeedParamsV2.prerender, required = false) prerender: Boolean?,
    @RequestParam(WebToFeedParamsV2.prerenderWaitUntil, required = false) prerenderWaitUntil: PuppeteerWaitUntil?,
    @RequestParam(WebToFeedParamsV2.prerenderScript, required = false) prerenderScript: String?,
    @RequestParam(WebToFeedParamsV2.version) version: String,
    @RequestParam(WebToFeedParamsV2.format, required = false) responseTypeParamV2: String?,
    // V1 params
    @RequestParam(WebToFeedParamsV1.url, required = false) urlV1: String?,
    @RequestParam(WebToFeedParamsV1.link, required = false) linkXPathV1: String?,
    @RequestParam(WebToFeedParamsV1.extendContext, required = false) extendContextV1: String?,
    @RequestParam(WebToFeedParamsV1.contextPath, required = false) contextXPathV1: String?,
    @RequestParam(WebToFeedParamsV1.datePath, required = false) dateXPathV1: String?,
    @RequestParam(WebToFeedParamsV1.format, required = false) responseTypeParamV1: String?,

    request: HttpServletRequest
  ): ResponseEntity<String> {
    meterRegistry.counter(AppMetrics.feedFromWeb, listOf(Tag.of("version", version))).increment()
    val url = StringUtils.trimToNull(urlV2) ?: urlV1!!
    val extendContext = StringUtils.trimToNull(extendContextV2) ?: extendContextV1
    val contextXPath = StringUtils.trimToNull(contextXPathV2) ?: contextXPathV1
    val dateXPath = StringUtils.trimToNull(dateXPathV2) ?: dateXPathV1
    val responseTypeParam = StringUtils.trimToNull(responseTypeParamV2) ?: responseTypeParamV1

    val corrId = handleCorrId(corrIdParam)
    val (responseType, convert) = feedExporter.resolveResponseType(corrId, responseTypeParam)

    log.info("[$corrId] w2f/$responseType url=$url")

    val selectors = GenericFeedSelectors(
      linkXPath = StringUtils.trimToNull(linkXPathV2) ?: linkXPathV1!!,
      contextXPath = contextXPath!!,
      paginationXPath = paginationXPath,
      extendContext = parseExtendContext(extendContext),
      dateXPath = dateXPath,
      dateIsStartOfEvent = dateIsStartOfEvent ?: false
    )
    val parserOptions = GenericFeedParserOptions(
      strictMode = strictMode ?: false,
      version = version,
    )
    val refineOptions = GenericFeedRefineOptions(
      filter = trimToNull(filter),
    )

    val scrapeRequest = ScrapeRequest.newBuilder()
      .elements(listOf("/"))
      .emit(listOf(ScrapeEmitType.markup))
      .page(ScrapePage.newBuilder()
        .url(url)
        .prerender(if (prerender == true) {
          ScrapePrerender.newBuilder()
            .evalScript(trimToNull(prerenderScript))
            .waitUntil(toDto(prerenderWaitUntil ?: PuppeteerWaitUntil.load))
            .build()
        } else {
          null
        })
        .build())
      .build()

    val feedUrl = webToFeedTransformer.createFeedUrl(URL(url), selectors, parserOptions, scrapeRequest, refineOptions)

    return runCatching {
      authService.assertToken(request)
      val topElement = scrapeService.scrape(corrId, scrapeRequest).block()!!.getRootElement()
      convert(
        webToFeedService.applyRule(corrId, url, feedUrl, selectors, topElement, parserOptions, refineOptions),
        HttpStatus.OK,
        1.toDuration(DurationUnit.HOURS)
      )
    }
      .getOrElse {
        if (it is HostOverloadingException) {
//          it.printStackTrace()
          log.warn("[$corrId] ${it.message}")
          throw it
        }
        if (debug == true) {
          log.error("[${corrId}] ${it.message}")
          val article = webToFeedService.createMaintenanceArticle(it, url)
          convert(
            webToFeedService.createMaintenanceFeed(corrId, url, feedUrl, article),
            HttpStatus.SERVICE_UNAVAILABLE,
            1.toDuration(DurationUnit.DAYS)
          )
        } else {
//          it.printStackTrace()
          log.error("[$corrId] ${it.message}")
          ResponseEntity.badRequest().body(it.message)
        }
      }
  }

  private fun trimToNull(value: String?): String? {
    return if (value == "null") {
      null
    } else {
      StringUtils.trimToNull(value)
    }
  }

  private fun parseExtendContext(extendContext: String?): ExtendContext {
    return if (StringUtils.isBlank(extendContext)) {
      ExtendContext.NONE
    } else {
      ExtendContext.values().first { it.value == extendContext }
    }
  }

}
