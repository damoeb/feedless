package org.migor.feedless.report

import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.reportDelete
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.injectCapabilitiesFromJwt
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
@Profile("${AppProfiles.report} & ${AppLayer.api}")
class ReportController(
  private val reportUseCase: ReportUseCase,
  private val jwtTokenIssuer: JwtTokenIssuer,
) {

  private val log = LoggerFactory.getLogger(ReportController::class.simpleName)

  @GetMapping(
    "${reportDelete}/{reportId}",
  )
  suspend fun deleteReport(
    request: HttpServletRequest,
    @PathVariable("reportId") reportId: String,
    @RequestParam("deleteReportJwt") deleteReportJwt: String,
  ): ResponseEntity<String> =
    withContext(context = injectCapabilitiesFromJwt(jwtTokenIssuer.decodeJwt(deleteReportJwt))) {
      log.info("GET deleteReport id=$reportId")
      reportUseCase.deleteReport(ReportId(reportId))

      ResponseEntity.ok()
        .body("report deleted")
    }
}
