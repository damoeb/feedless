package org.migor.feedless.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.reportDelete
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.TokenIssuer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * The cancel link in every report mail. It works without login, since recipients usually have no
 * account: the signed token is the proof, and it must name the report in the path so a valid token
 * cannot cancel someone else's report.
 */
@Controller
@Profile("${AppProfiles.report} & ${AppLayer.api}")
class ReportController(
  private val reportUseCase: ReportUseCase,
  private val tokenIssuer: TokenIssuer,
) {

  private val log = LoggerFactory.getLogger(ReportController::class.simpleName)

  @GetMapping("$reportDelete/{reportId}")
  suspend fun deleteReport(
    @PathVariable("reportId") reportId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET deleteReport id=$reportId")
    requireTokenNames(token, reportId)
    reportUseCase.deleteReportFromToken(ReportId(reportId))
    return ResponseEntity.ok().body("report deleted")
  }

  private suspend fun requireTokenNames(token: String, reportId: String) {
    val claimed = runCatching { tokenIssuer.decodeJwt(token) }
      .getOrElse { throw AccessDeniedException("invalid token") }
      .getClaimAsString(JwtParameterNames.REPORT_ID)

    if (claimed != reportId) {
      throw AccessDeniedException("token does not name this report")
    }
  }
}
