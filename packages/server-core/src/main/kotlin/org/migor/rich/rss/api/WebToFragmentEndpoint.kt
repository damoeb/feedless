package org.migor.rich.rss.api

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import org.migor.rich.rss.auth.AuthService
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.transform.FetchOptions
import org.migor.rich.rss.transform.PuppeteerEmitType
import org.migor.rich.rss.transform.PuppeteerWaitUntil
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


enum class WebFragmentType {
  markup, text, pixel
}

@RestController
class WebToFragmentEndpoint {

  private val log = LoggerFactory.getLogger(WebToFragmentEndpoint::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var httpService: HttpService

  // http://localhost:8080/api/w2frag?u=https%3A%2F%2Fheise.de&x=%2F&t=text
  @Throttled
  @Timed
  @GetMapping(ApiUrls.webToFragment)
  fun handle(
    @RequestParam(ApiParams.corrId, required = false) corrIdParam: String?,
    @RequestParam(WebToPageChangeParams.url) url: String,
    @RequestParam(WebToPageChangeParams.xpath) xpath: String,
    @RequestParam(WebToPageChangeParams.prerender, required = false, defaultValue = "false") prerender: Boolean,
    @RequestParam(WebToPageChangeParams.prerenderWaitUntil, required = false) prerenderWaitUntil: PuppeteerWaitUntil?,
    @RequestParam(WebToPageChangeParams.prerenderScript, required = false) prerenderScript: String?,
    @RequestParam(WebToPageChangeParams.version) version: String,
    @RequestParam(WebToPageChangeParams.type) fragmentType: WebFragmentType,
    request: HttpServletRequest
  ): ResponseEntity<WebFragment> {
    meterRegistry.counter("w2pc", listOf(Tag.of("version", version))).increment()

    val corrId = handleCorrId(corrIdParam)

    // todo verify token
    log.info("[$corrId] ${ApiUrls.webToFragment} url=$url")

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
    return ResponseEntity.ok(WebFragment(
      url = httpResponse.url,
      statusCode = httpResponse.statusCode,
      responseBody = String(httpResponse.responseBody)
    ))
  }
}

data class WebFragment (
//  val contentType: String,
  val url: String,
  val statusCode: Int,
  val responseBody: String
)
