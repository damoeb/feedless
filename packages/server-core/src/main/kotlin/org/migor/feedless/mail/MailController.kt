package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.mailForwardingAllow
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.*

@Controller
@Profile("${AppProfiles.mail} & ${AppLayer.api}")
class MailController(
  private val mailService: MailService,
  private val templateService: TemplateService
) {

  private val log = LoggerFactory.getLogger(MailController::class.simpleName)

  @GetMapping(
    "${mailForwardingAllow}/{mailForwardId}",
  )
  suspend fun mailForwardingAllow(
    request: HttpServletRequest,
    @PathVariable("mailForwardId") mailForwardId: String,
  ): ResponseEntity<String> = coroutineScope {
    log.info("GET authorizeMailForward id=$mailForwardId")
    mailService.updateMailForwardById(UUID.fromString(mailForwardId), true)

    ResponseEntity.ok()
      .body(templateService.renderTemplate(MailTrackerAuthorizedTemplate()))
  }
}
