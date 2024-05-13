package org.migor.feedless.attachment

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.HttpService
import org.migor.feedless.util.HttpUtil.createCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile(AppProfiles.database)
class AttachmentController {

  private val log = LoggerFactory.getLogger(AttachmentController::class.simpleName)

  @Autowired
  lateinit var attachmentService: AttachmentService

  @Autowired
  lateinit var httpService: HttpService

  @GetMapping(
    "/attachment/{attachmentId}",
    "/a/{attachmentId}"
  )
  fun attachmentById(
    request: HttpServletRequest,
    @PathVariable("attachmentId") attachmentId: String,
  ): ResponseEntity<ByteArray> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET attachmentId id=$attachmentId")
    val attachment = attachmentService.findById(attachmentId)

    return if (attachment.isPresent) {
      val a = attachment.get()
      ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, a.contentType)
        .body(a.data)
    } else {
      ResponseEntity.notFound().build()
    }
  }

  @GetMapping(
    "/attachment/proxy",
  )
  fun attachmentProxy(
    request: HttpServletRequest,
    @RequestParam("url") url: String,
  ): ResponseEntity<ByteArray> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET proxy attachment url=$url")
    val attachment = httpService.httpGet(corrId, url, 200)
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_TYPE, attachment.contentType)
      .body(attachment.responseBody)
  }

}
