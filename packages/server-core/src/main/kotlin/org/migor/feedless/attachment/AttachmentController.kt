package org.migor.feedless.attachment

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.Tracked
import org.migor.feedless.common.HttpService
import org.migor.feedless.util.HttpUtil.createCorrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.attachment} & ${AppLayer.api}")
class AttachmentController(
  private val attachmentService: AttachmentService,
  private val httpService: HttpService
) {

  private val log = LoggerFactory.getLogger(AttachmentController::class.simpleName)

  @Tracked
  @GetMapping(
    "/attachment/{attachmentId}",
  )
  suspend fun attachmentById(
    request: HttpServletRequest,
    @PathVariable("attachmentId") attachmentId: String,
  ): ResponseEntity<ByteArray> = coroutineScope {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET attachmentId id=$attachmentId")
    val attachment = attachmentService.findById(attachmentId)

    if (attachment.isPresent) {
      val a = attachment.get()
      ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, a.mimeType)
        .body(a.data)
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
