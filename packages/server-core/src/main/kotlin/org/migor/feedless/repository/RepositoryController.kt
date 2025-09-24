package org.migor.feedless.repository

import com.google.gson.Gson
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.generated.types.RecordOrderByInput
import org.migor.feedless.generated.types.RecordsWhereInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@Controller
@Transactional(propagation = Propagation.NEVER)
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

  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/f/{repositoryId}/{format}"],
    produces = [
      "application/atom+xml;charset=UTF-8",
      "text/calendar;charset=UTF-8",
      "application/json;charset=UTF-8"
    ]
  )
  suspend fun feed(
    @PathVariable(name = "repositoryId") repositoryId: String,
    @PathVariable(name = "format") format: String,
    @RequestParam(value = "page", required = false, defaultValue = "0") page: Int,
    @RequestParam(value = "tags", required = false) tags: List<String>,
    @RequestParam(value = "where", required = false) whereStr: String?,
    @RequestParam(value = "orderByStr", required = false) orderByStr: String?,
  ): ResponseEntity<String> = coroutineScope {
    meterRegistry.counter(
      AppMetrics.fetchRepository, listOf(
        Tag.of("type", "repository"),
        Tag.of("id", repositoryId),
      )
    ).increment()
    log.debug("GET feed/$format} id=$repositoryId page=$page")

    feedExporter.to(
      HttpStatus.OK,
      format,
      repositoryService.getFeedByRepositoryId(
        RepositoryId(repositoryId),
        page,
        tags,
        parseWhere(whereStr),
        parseOrderBy(orderByStr)
      )
    )
  }

  private fun parseWhere(whereStr: String?): RecordsWhereInput? {
    return whereStr?.let {
      Gson().fromJson(it, RecordsWhereInput::class.java)
    }
  }

  private fun parseOrderBy(orderByStr: String?): RecordOrderByInput? {
    return orderByStr?.let {
      Gson().fromJson(it, RecordOrderByInput::class.java)
    }
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
