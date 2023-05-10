package org.migor.feedless.api.http

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.WebToPageChangeParams
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.HttpService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.migor.feedless.util.JsonUtil
import org.migor.feedless.web.FetchOptions
import org.migor.feedless.web.PuppeteerEmitType
import org.migor.feedless.web.PuppeteerWaitUntil
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


enum class WebFragmentType {
  markup, text, pixel
}

@RestController
class WebToFragmentEndpoint {

  private val log = LoggerFactory.getLogger(WebToFragmentEndpoint::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var feedExporter: FeedExporter

  // http://localhost:8080/api/w2frag?u=https%3A%2F%2Fheise.de&x=%2F&t=text
  @Throttled
  @Timed
  @GetMapping(ApiUrls.webToFeedFromChange)
  fun handle(
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam(WebToPageChangeParams.url) url: String,
    @RequestParam(WebToPageChangeParams.xpath) xpath: String,
    @RequestParam(WebToPageChangeParams.prerender, required = false, defaultValue = "false") prerender: Boolean,
    @RequestParam(WebToPageChangeParams.prerenderWaitUntil, required = false) prerenderWaitUntil: PuppeteerWaitUntil?,
    @RequestParam(WebToPageChangeParams.prerenderScript, required = false) prerenderScript: String?,
    @RequestParam(WebToPageChangeParams.version) version: String,
    @RequestParam(WebToPageChangeParams.type) fragmentType: WebFragmentType,
    @RequestParam(WebToPageChangeParams.format, required = false) responseTypeParam: String?,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    meterRegistry.counter("w2f/c", listOf(Tag.of("version", version))).increment()

    val corrId = handleCorrId(corrIdParam)

    // todo verify token
    log.info("[$corrId] ${ApiUrls.webToFeedFromChange} url=$url")

    val fetchOptions = FetchOptions(
      websiteUrl = url,
      prerender = prerender,
      emit = when(fragmentType) {
        WebFragmentType.markup -> PuppeteerEmitType.markup
        WebFragmentType.text -> PuppeteerEmitType.text
        WebFragmentType.pixel -> PuppeteerEmitType.pixel
      },
      prerenderWaitUntil = prerenderWaitUntil ?: PuppeteerWaitUntil.load,
      prerenderScript = prerenderScript
    )

    val httpResponse = httpService.httpGetCaching(corrId, fetchOptions).blockFirst()!!
    val webFragment = WebFragment(
      url = httpResponse.url,
      statusCode = httpResponse.statusCode,
      responseBody = String(httpResponse.responseBody)
    )

    val feed = RichFeed()
    feed.title = "Page Change of ${URL(url).host}"
    val article = RichArticle()
    article.contentRaw = webFragment.responseBody
    article.contentRawMime = if (fragmentType === WebFragmentType.pixel) "image/png;base64" else if (fragmentType === WebFragmentType.markup) "text/html" else "text/plain"

    feed.items = listOf(article)

    return responseTypeParam?.let {
      feedExporter.resolveResponseType(corrId, responseTypeParam).second(
        feed,
        HttpStatus.OK,
        1.toDuration(DurationUnit.HOURS)
      )
    } ?: ResponseEntity.ok(JsonUtil.gson.toJson(webFragment))
  }
}

data class WebFragment (
//  val contentType: String,
  val url: String,
  val statusCode: Int,
  val responseBody: String
)
