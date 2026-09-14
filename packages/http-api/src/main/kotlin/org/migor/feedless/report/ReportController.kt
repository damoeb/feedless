package org.migor.feedless.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.reportAbuse
import org.migor.feedless.api.ApiUrls.reportConfirm
import org.migor.feedless.api.ApiUrls.reportDelete
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.template.PageTemplateReportAbuse
import org.migor.feedless.template.TemplateService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * The links in report mails. They work without login, since recipients usually have no account: the signed token is
 * the proof, and it must name the id in the path so a valid token cannot act on someone else's report or address.
 */
@Controller
@Profile("${AppProfiles.report} & ${AppLayer.api}")
class ReportController(
  private val reportUseCase: ReportUseCase,
  private val tokenIssuer: TokenIssuer,
  private val templateService: TemplateService,
) {

  private val log = LoggerFactory.getLogger(ReportController::class.simpleName)

  @GetMapping("$reportDelete/{reportId}")
  suspend fun deleteReport(
    @PathVariable("reportId") reportId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET deleteReport id=$reportId")
    requireClaim(token, JwtParameterNames.REPORT_ID, reportId)
    reportUseCase.deleteReportFromToken(ReportId(reportId))
    return ResponseEntity.ok().body("report deleted")
  }

  @GetMapping("$reportConfirm/{reportId}")
  suspend fun confirmReport(
    @PathVariable("reportId") reportId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET confirmReport id=$reportId")
    requireClaim(token, JwtParameterNames.REPORT_ID, reportId)
    reportUseCase.confirmReportFromToken(ReportId(reportId))
    return ResponseEntity.ok().body("report confirmed")
  }

  @GetMapping("$reportAbuse/{recipientId}")
  suspend fun reportAbuse(
    @PathVariable("recipientId") recipientId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET reportAbuse recipientId=$recipientId")
    requireClaim(token, JwtParameterNames.RECIPIENT_ID, recipientId)
    reportUseCase.reportAbuse(ReportRecipientId(recipientId))
    return ResponseEntity.ok()
      .contentType(MediaType.TEXT_HTML)
      .body(templateService.renderTemplate(PageTemplateReportAbuse()))
  }

  private suspend fun requireClaim(token: String, claim: String, expected: String) {
    val claimed = runCatching { tokenIssuer.decodeJwt(token) }
      .getOrElse { throw AccessDeniedException("invalid token") }
      .getClaimAsString(claim)

    if (claimed != expected) {
      throw AccessDeniedException("token does not name this link's target")
    }
  }
}
