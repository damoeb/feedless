package org.migor.feedless.api.http

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.service.SourceSubscriptionService
import org.migor.feedless.util.HttpUtil.createCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.ResourceUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.nio.file.Files

@Controller
@Profile(AppProfiles.database)
class SourceSubscriptionController {

  private val log = LoggerFactory.getLogger(SourceSubscriptionController::class.simpleName)

  @Autowired
  lateinit var sourceSubscriptionService: SourceSubscriptionService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var feedExporter: FeedExporter

  @GetMapping(
    "/feed/{subscriptionId}/atom",
    "/f/{subscriptionId}/atom", produces = ["application/atom+xml;charset=UTF-8"]
  )
  fun atomFeed(
    request: HttpServletRequest,
    @PathVariable("subscriptionId") subscriptionId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    meterRegistry.counter(
      AppMetrics.fetchFeed, listOf(
        Tag.of("type", "subscription"),
        Tag.of("page", page.toString()),
        Tag.of("id", subscriptionId),
        Tag.of("format", "atom")
      )
    ).increment()
    log.info("[$corrId] GET feed/atom id=$subscriptionId page=$page")
    return feedExporter.to(
      corrId,
      HttpStatus.OK,
      "atom",
      sourceSubscriptionService.getFeedBySubscriptionId(subscriptionId, page)
    )
  }

  @GetMapping(
    "/feed/{subscriptionId}/json",
    "/feed/{subscriptionId}",
    "/f/{subscriptionId}/json",
    "/f/{subscriptionId}",
    produces = ["application/json;charset=UTF-8"]
  )
  fun jsonFeed(
    request: HttpServletRequest,
    @PathVariable("subscriptionId") subscriptionId: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    meterRegistry.counter(
      AppMetrics.fetchFeed, listOf(
        Tag.of("type", "subscription"),
        Tag.of("id", subscriptionId),
        Tag.of("page", page.toString()),
        Tag.of("format", "json")
      )
    ).increment()
    log.info("[$corrId] GET feed/json id=$subscriptionId page=$page")
    return feedExporter.to(
      corrId,
      HttpStatus.OK,
      "json",
      sourceSubscriptionService.getFeedBySubscriptionId(subscriptionId, page)
    )
  }

  @GetMapping(
    "/feed/static/feed.xsl", produces = ["text/xsl"]
  )
  fun xsl(request: HttpServletRequest): ResponseEntity<String> {
    return ResponseEntity.ok(Files.readString(ResourceUtils.getFile("classpath:feed.xsl").toPath()))
  }
}
