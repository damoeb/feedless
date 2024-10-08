package org.migor.feedless.document

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.Tracked
import org.migor.feedless.session.useRequestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Controller
@Profile(AppProfiles.api)
class DocumentController {

  private val log = LoggerFactory.getLogger(DocumentController::class.simpleName)

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var meterRegistry: MeterRegistry

  @Tracked
  @GetMapping(
    "/article/{documentId}",
    "/a/{documentId}",
  )
  suspend fun documentById(
    request: HttpServletRequest,
    @PathVariable("documentId") documentId: String,
  ): ResponseEntity<String> = withContext(useRequestContext(currentCoroutineContext())) {
    documentService.findById(UUID.fromString(documentId))?.let { document ->
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
