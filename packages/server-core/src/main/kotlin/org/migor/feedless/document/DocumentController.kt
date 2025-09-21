package org.migor.feedless.document

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.AnalyticsService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.document} & ${AppLayer.api}")
class DocumentController(
  private val documentService: DocumentService,
  private val meterRegistry: MeterRegistry,
  private val analyticsService: AnalyticsService
) {

  private val log = LoggerFactory.getLogger(DocumentController::class.simpleName)

  @GetMapping(
    "article/{documentId}",
    "a/{documentId}",
  )
  suspend fun documentById(
    request: HttpServletRequest,
    @PathVariable("documentId") documentId: String,
  ): ResponseEntity<String> = coroutineScope {
    analyticsService.track()
    documentService.findById(DocumentId(documentId))?.let { document ->
      meterRegistry.counter(
        AppMetrics.fetchRepository, listOf(
          Tag.of("type", "document"),
          Tag.of("id", documentId),
        )
      ).increment()
      log.debug("GET document id=$documentId")

      val url = request.getParameter("source")?.let {
        "${document.url}?source=${URLEncoder.encode(it, StandardCharsets.UTF_8)}"
      } ?: document.url

      val headers = HttpHeaders()
      headers.add(HttpHeaders.LOCATION, url)
      ResponseEntity<String>(headers, HttpStatus.FOUND)
    } ?: ResponseEntity.notFound().build()
  }

}
