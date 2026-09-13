package org.migor.feedless.attachment

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.analytics.Analytics
import org.migor.feedless.common.HttpFetcher
import org.migor.feedless.document.DocumentGuard
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
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
  private val httpFetcher: HttpFetcher,
  private val documentGuard: DocumentGuard,
  private val analytics: Analytics
) {

  private val log = LoggerFactory.getLogger(AttachmentController::class.simpleName)

  @GetMapping(
    "/attachment/{attachmentId}",
  )
  suspend fun attachmentById(
    request: HttpServletRequest,
    @PathVariable("attachmentId") attachmentId: String,
  ): ResponseEntity<ByteArray> = withContext(injectCapabilitiesFromSecurityContext()) {
    analytics.track()
    val (attachment, data) = attachmentUseCase.findByIdWithData(AttachmentId(attachmentId))

    if (attachment.isPresent && data != null && mayRead(attachment.get())) {
      val a = attachment.get()
      ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, a.mimeType)
        .body(data)
    } else {
      ResponseEntity.notFound().build()
    }
  }

  // private feeds cannot carry the share key to attachment links, so only owner and members get private attachments
  private suspend fun mayRead(attachment: Attachment): Boolean = try {
    documentGuard.requireRead(attachment.documentId)
    true
  } catch (e: NotFoundException) {
    false
  }

  @GetMapping(
    "/attachment/proxy",
  )
  suspend fun attachmentProxy(
    request: HttpServletRequest,
    @RequestParam("url") url: String,
  ): ResponseEntity<ByteArray> = coroutineScope {
    log.debug("GET proxy attachment url=$url")
    val attachment = httpFetcher.httpGet(url, 200)
    ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_TYPE, attachment.contentType)
      .body(attachment.responseBody)
  }

}
