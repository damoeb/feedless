package org.migor.feedless.api.http

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.WebDocumentService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.HttpUtil.createCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Deprecated("obsolete")
@Profile(AppProfiles.database)
class StreamController {

  private val log = LoggerFactory.getLogger(StreamController::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var feedExporter: FeedExporter

  @GetMapping("/stream/{streamId}/atom", produces = ["application/atom+xml;charset=UTF-8"])
  fun atomFeed(
    request: HttpServletRequest,
    @PathVariable("streamId") streamId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET stream/atom id=$streamId page=$page")
    meterRegistry.counter(
      AppMetrics.fetchFeed, listOf(
        Tag.of("type", "stream"),
        Tag.of("id", streamId),
        Tag.of("page", page.toString()),
        Tag.of("format", "atom")
      )
    ).increment()

    return feedExporter.to(corrId, HttpStatus.OK, "atom", feedService.findByStreamId(streamId, page))
  }

  @GetMapping(
    "/stream/{streamId}",
    "/stream/{streamId}/json",
    produces = ["application/json;charset=UTF-8"]
  )
  fun jsonFeed(
    request: HttpServletRequest,
    @PathVariable("streamId") streamId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET stream/json id=$streamId page=$page")
    meterRegistry.counter(
      AppMetrics.fetchFeed, listOf(
        Tag.of("type", "stream"),
        Tag.of("id", streamId),
        Tag.of("page", page.toString()),
        Tag.of("format", "json")
      )
    ).increment()

    return feedExporter.to(newCorrId(), HttpStatus.OK, "json", feedService.findByStreamId(streamId, page))
  }
}
