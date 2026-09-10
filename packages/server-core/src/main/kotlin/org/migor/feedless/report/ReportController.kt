package org.migor.feedless.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.reportConfirm
import org.migor.feedless.api.ApiUrls.reportDelete
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.JwtTokenIssuer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * Die beiden Links aus den Report-Mails.
 *
 * Beide laufen ohne Anmeldung: der Empfänger hat typischerweise kein Konto.
 * Der Nachweis ist das signierte Token, und es nennt genau einen Report. Die
 * Report-Id aus dem Pfad wird gegen die aus dem Token geprüft, damit ein
 * gültiges Token nicht auf einen fremden Report angewendet werden kann.
 *
 * Vorher lief das Löschen über injectCapabilitiesFromJwt und den ReportGuard,
 * dessen Methoden alle NotImplementedError warfen - der Endpunkt scheiterte
 * also zur Laufzeit.
 */
@Controller
@Profile("${AppProfiles.report} & ${AppLayer.api}")
class ReportController(
  private val reportUseCase: ReportUseCase,
  private val jwtTokenIssuer: JwtTokenIssuer,
) {

  private val log = LoggerFactory.getLogger(ReportController::class.simpleName)

  @GetMapping("$reportConfirm/{reportId}")
  suspend fun confirmReport(
    @PathVariable("reportId") reportId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET confirmReport id=$reportId")
    requireTokenNames(token, reportId)
    reportUseCase.confirmReportFromToken(ReportId(reportId))
    return ResponseEntity.ok().body("report confirmed")
  }

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
    val claimed = runCatching { jwtTokenIssuer.decodeJwt(token) }
      .getOrElse { throw AccessDeniedException("invalid token") }
      .getClaimAsString(JwtParameterNames.REPORT_ID)

    if (claimed != reportId) {
      throw AccessDeniedException("token does not name this report")
    }
  }
}
