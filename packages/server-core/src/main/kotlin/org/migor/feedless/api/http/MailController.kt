package org.migor.feedless.api.http

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.mailForwardingAllow
import org.migor.feedless.service.MailService
import org.migor.feedless.service.MailTrackerAuthorizedTemplate
import org.migor.feedless.service.TemplateService
import org.migor.feedless.util.HttpUtil.createCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@Controller
@Profile("${AppProfiles.database} & ${AppProfiles.mail}")
class MailController {

  private val log = LoggerFactory.getLogger(MailController::class.simpleName)

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var templateService: TemplateService

  @GetMapping(
    "${mailForwardingAllow}/{mailForwardId}",
  )
  fun mailForwardingAllow(
    request: HttpServletRequest,
    @PathVariable("mailForwardId") mailForwardId: String,
  ): ResponseEntity<String> {
    val corrId = createCorrId(request)
    log.info("[$corrId] GET authorizeMailForward id=$mailForwardId")
    mailService.updateMailForwardById(UUID.fromString(mailForwardId), true)

    return ResponseEntity.ok()
      .body(templateService.renderTemplate(corrId, MailTrackerAuthorizedTemplate()))
  }
}
