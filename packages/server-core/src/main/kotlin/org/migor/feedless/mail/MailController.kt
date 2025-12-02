package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.mailForwardingAllow
import org.migor.feedless.mail.template.FreemarkerTemplateService
import org.migor.feedless.report.ReportId
import org.migor.feedless.report.ReportUseCase
import org.migor.feedless.template.MailTemplateChangeTrackerAuthorized
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
@Profile("${AppProfiles.mail} & ${AppLayer.api}")
class MailController(
  private val reportUseCase: ReportUseCase,
  private val templateService: FreemarkerTemplateService
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
    reportUseCase.updateReportById(ReportId(mailForwardId), true)

    ResponseEntity.ok()
      .body(templateService.renderTemplate(MailTemplateChangeTrackerAuthorized()))
  }
}
