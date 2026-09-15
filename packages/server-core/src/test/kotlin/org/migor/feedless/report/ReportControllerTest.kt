package org.migor.feedless.report

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.template.PageTemplateReportAbuse
import org.migor.feedless.template.TemplateService
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt

/** The links in report mails; recipients usually have no account, so the signed token is the proof. */
class ReportControllerTest {

  private val reportId = ReportId()
  private val recipientId = ReportRecipientId()
  private lateinit var reportUseCase: ReportUseCase
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var templateService: TemplateService
  private lateinit var controller: ReportController

  private fun tokenWith(claim: String, value: String): Jwt = Jwt.withTokenValue("token")
    .header("alg", "HS256")
    .claim(claim, value)
    .build()

  private fun tokenDecodesTo(jwt: Jwt) {
    jwtTokenIssuer.stub { onBlocking { decodeJwt(any<String>()) } doReturn jwt }
  }

  @BeforeEach
  fun setUp() {
    reportUseCase = mock()
    jwtTokenIssuer = mock()
    templateService = mock()
    controller = ReportController(reportUseCase, jwtTokenIssuer, templateService)
  }

  @Test
  fun `cancels a report through the link in its mail`() = runTest {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, reportId.uuid.toString()))

    controller.deleteReport(reportId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { deleteReportFromToken(reportId) }
  }

  @Test
  fun `confirms a report through the link in its confirmation request`() = runTest {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, reportId.uuid.toString()))

    controller.confirmReport(reportId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { confirmReportFromToken(reportId) }
  }

  @Test
  fun `reports abuse through the link and answers with a page`() = runTest {
    tokenDecodesTo(tokenWith(JwtParameterNames.RECIPIENT_ID, recipientId.uuid.toString()))
    templateService.stub { on { renderTemplate(any<PageTemplateReportAbuse>()) } doReturn "<p>danke</p>" }

    val response = controller.reportAbuse(recipientId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { reportAbuse(recipientId) }
    assertThat(response.body).isEqualTo("<p>danke</p>")
  }

  /** Without this check the link from one's own mail could cancel anyone's report. */
  @Test
  fun `rejects a valid token that names another report`() {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, ReportId().uuid.toString()))

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.deleteReport(reportId.uuid.toString(), "token") }
    }
    verifyBlocking(reportUseCase, never()) { deleteReportFromToken(any()) }
  }

  @Test
  fun `rejects an abuse token that names another recipient`() {
    tokenDecodesTo(tokenWith(JwtParameterNames.RECIPIENT_ID, ReportRecipientId().uuid.toString()))

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.reportAbuse(recipientId.uuid.toString(), "token") }
    }
    verifyBlocking(reportUseCase, never()) { reportAbuse(any()) }
  }

  @Test
  fun `rejects a confirm token that names another report`() {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, ReportId().uuid.toString()))

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.confirmReport(reportId.uuid.toString(), "token") }
    }
    verifyBlocking(reportUseCase, never()) { confirmReportFromToken(any()) }
  }

  @Test
  fun `rejects a report token on the abuse link`() {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, recipientId.uuid.toString()))

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.reportAbuse(recipientId.uuid.toString(), "token") }
    }
    verifyBlocking(reportUseCase, never()) { reportAbuse(any()) }
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
