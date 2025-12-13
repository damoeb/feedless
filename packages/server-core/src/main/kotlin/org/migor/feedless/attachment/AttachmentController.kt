package org.migor.feedless.attachment

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.common.HttpService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile("${AppProfiles.attachment} & ${AppLayer.api}")
class AttachmentController(
  private val attachmentUseCase: AttachmentUseCase,
  private val httpService: HttpService,
  private val analyticsService: AnalyticsService
) {

  private val log = LoggerFactory.getLogger(AttachmentController::class.simpleName)

  @GetMapping(
    "/attachment/{attachmentId}",
  )
  suspend fun attachmentById(
    request: HttpServletRequest,
    @PathVariable("attachmentId") attachmentId: String,
  ): ResponseEntity<ByteArray> = coroutineScope {
    analyticsService.track()
    val (attachment, data) = attachmentUseCase.findByIdWithData(AttachmentId(attachmentId))

    if (attachment.isPresent && data != null) {
      val a = attachment.get()
      ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, a.mimeType)
        .body(data)
    } else {
      ResponseEntity.notFound().build()
    }
  }

  @GetMapping(
    "/attachment/proxy",
  )
  suspend fun attachmentProxy(
    request: HttpServletRequest,
    @RequestParam("url") url: String,
  ): ResponseEntity<ByteArray> = coroutineScope {
    log.debug("GET proxy attachment url=$url")
    val attachment = httpService.httpGet(url, 200)
    ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_TYPE, attachment.contentType)
      .body(attachment.responseBody)
  }

}
