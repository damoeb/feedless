package org.migor.feedless.document

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.Tracked
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@Controller
@Profile(AppProfiles.database)
class DocumentController {

  private val log = LoggerFactory.getLogger(DocumentController::class.simpleName)

  @Autowired
  lateinit var documentService: DocumentService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Tracked
  @GetMapping(
    "/article/{documentId}",
    "/a/{documentId}",
  )
  fun documentById(
    request: HttpServletRequest,
    @PathVariable("documentId") documentId: String,
  ): ResponseEntity<String> {
    return documentService.findById(UUID.fromString(documentId))?.let {
      meterRegistry.counter(
        AppMetrics.fetchRepository, listOf(
          Tag.of("type", "document"),
          Tag.of("id", documentId),
        )
      ).increment()
      log.debug("GET document id=$documentId")

      val headers = HttpHeaders()
      headers.add(HttpHeaders.LOCATION, it.url)
      ResponseEntity<String>(headers, HttpStatus.FOUND)
    } ?: ResponseEntity.notFound().build()
  }

}
