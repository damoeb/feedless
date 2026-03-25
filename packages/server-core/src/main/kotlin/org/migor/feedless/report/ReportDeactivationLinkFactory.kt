package org.migor.feedless.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.config.AppUrlsProperties
import org.migor.feedless.session.JwtTokenIssuer
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class ReportDeactivationLinkFactory(
  private val appUrlsProperties: AppUrlsProperties,
  private val jwtTokenIssuer: JwtTokenIssuer,
) {

  suspend fun createLink(report: Report): String {
    val userId = report.userId
      ?: throw IllegalArgumentException("report.userId is required for deactivation link")
    val jwt = jwtTokenIssuer.createJwtForReportDeactivationLink(userId)
    val token = URLEncoder.encode(jwt.tokenValue, StandardCharsets.UTF_8)
    val base = appUrlsProperties.apiGatewayUrl.trimEnd('/')
    return "$base${ApiUrls.reportDelete}/${report.id.uuid}?deleteReportJwt=$token"
  }
}
