package org.migor.feedless.repository

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.Tracked
import org.migor.feedless.api.ApiParams
import org.migor.feedless.feed.exporter.FeedExporter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@Controller
@Profile("${AppProfiles.repository} & ${AppLayer.api}")
class RepositoryController {

  private lateinit var feedXsl: String
  private val log = LoggerFactory.getLogger(RepositoryController::class.simpleName)

  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Autowired
  private lateinit var meterRegistry: MeterRegistry

  @Autowired
  private lateinit var feedExporter: FeedExporter

  @Tracked
  @GetMapping(
    "/f/{repositoryId}/atom", produces = ["application/atom+xml;charset=UTF-8"]
  )
  suspend fun atomFeed(
    request: HttpServletRequest,
    @PathVariable("repositoryId") repositoryId: String,
    @RequestParam(ApiParams.page, required = false, defaultValue = "0") page: Int,
    @RequestParam("skey", required = false) shareKey: String? = null,
    @RequestParam(ApiParams.tag, required = false) tag: String?
  ): ResponseEntity<String> = coroutineScope {
    meterRegistry.counter(
      AppMetrics.fetchRepository, listOf(
        Tag.of("type", "repository"),
        Tag.of("id", repositoryId),
      )
    ).increment()
    log.debug("GET feed/atom id=$repositoryId page=$page")
    feedExporter.to(
      HttpStatus.OK,
      "atom",
      repositoryService.getFeedByRepositoryId(repositoryId, page, tag, shareKey)
    )
  }

  @Tracked
  @GetMapping(
//    "/feed/{repositoryId}/json",
//    "/feed/{repositoryId}",
    "/f/{repositoryId}/json",
    "/f/{repositoryId}",
    produces = ["application/json;charset=UTF-8"]
  )
  suspend fun jsonFeed(
    request: HttpServletRequest,
    @PathVariable("repositoryId") repositoryId: String,
    @RequestParam("skey", required = false) shareKey: String? = null,
    @RequestParam(ApiParams.page, required = false, defaultValue = "0") page: Int,
    @RequestParam(ApiParams.tag, required = false) tag: String?
  ): ResponseEntity<String> = coroutineScope {
    meterRegistry.counter(
      AppMetrics.fetchRepository, listOf(
        Tag.of("type", "repository"),
        Tag.of("id", repositoryId),
      )
    ).increment()
    log.debug("GET feed/json id=$repositoryId page=$page tag=$tag")
    feedExporter.to(
      HttpStatus.OK,
      "json",
      repositoryService.getFeedByRepositoryId(repositoryId, page, tag, shareKey)
    )
  }

  @GetMapping(
    "/feed/static/feed.xsl", produces = ["text/xsl"]
  )
  fun xsl(request: HttpServletRequest): ResponseEntity<String> {
    return ResponseEntity.ok(feedXsl)
  }

  @PostConstruct
  fun postConstruct() {
    val scanner = Scanner(ClassPathResource("/feed.xsl", this.javaClass.classLoader).inputStream)
    val data = StringBuilder()
    while (scanner.hasNextLine()) {
      data.appendLine(scanner.nextLine())
    }
    feedXsl = data.toString()
  }
}
