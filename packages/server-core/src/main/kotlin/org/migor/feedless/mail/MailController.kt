package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.mailForwardingAllow
import org.migor.feedless.report.ReportId
import org.migor.feedless.report.ReportService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.mail} & ${AppLayer.api}")
class MailController(
  private val reportService: ReportService,
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
    reportService.updateReportById(ReportId(mailForwardId), true)

    ResponseEntity.ok()
      .body(templateService.renderTemplate(ChangeTrackerAuthorizedTemplate()))
  }
}
