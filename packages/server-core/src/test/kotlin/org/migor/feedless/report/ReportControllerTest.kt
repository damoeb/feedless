package org.migor.feedless.report

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.JwtTokenIssuer
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Die beiden Links aus den Report-Mails. Ihre Empfänger haben meist kein
 * Konto; der Besitz des signierten Tokens ist der Nachweis. Diese Fälle ersetzen
 * die früheren leeren Rümpfe "report can be deleted by anonymous if created by
 * anonymous" und "report can be deleted without authorization".
 */
class ReportControllerTest {

  private val reportId = ReportId()
  private lateinit var reportUseCase: ReportUseCase
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var controller: ReportController

  private fun tokenNaming(id: ReportId): Jwt = Jwt.withTokenValue("token")
    .header("alg", "HS256")
    .claim(JwtParameterNames.REPORT_ID, id.uuid.toString())
    .build()

  private fun tokenDecodesTo(jwt: Jwt) {
    jwtTokenIssuer.stub { onBlocking { decodeJwt(any<String>()) } doReturn jwt }
  }

  @BeforeEach
  fun setUp() {
    reportUseCase = mock()
    jwtTokenIssuer = mock()
    controller = ReportController(reportUseCase, jwtTokenIssuer)
  }

  @Test
  fun `report can be deleted by anonymous through the link in its mail`() = runTest {
    tokenDecodesTo(tokenNaming(reportId))

    controller.deleteReport(reportId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { deleteReportFromToken(reportId) }
  }

  @Test
  fun `confirms a report through the link in its mail`() = runTest {
    tokenDecodesTo(tokenNaming(reportId))

    controller.confirmReport(reportId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { confirmReportFromToken(reportId) }
  }

  /**
   * Das Token ist gültig, nennt aber einen anderen Report. Ohne diesen
   * Abgleich liesse sich mit dem Link aus der eigenen Mail jedes fremde Abo
   * abbestellen.
   */
  @Test
  fun `rejects a valid token that names another report`() {
    tokenDecodesTo(tokenNaming(ReportId()))

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.deleteReport(reportId.uuid.toString(), "token") }
    }
    verifyBlocking(reportUseCase, never()) { deleteReportFromToken(any()) }
  }

  @Test
  fun `rejects a token that does not decode`() {
    jwtTokenIssuer.stub {
      onBlocking { decodeJwt(any<String>()) } doThrow IllegalArgumentException("bad signature")
    }

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.confirmReport(reportId.uuid.toString(), "forged") }
    }
    verifyBlocking(reportUseCase, never()) { confirmReportFromToken(any()) }
  }
}
