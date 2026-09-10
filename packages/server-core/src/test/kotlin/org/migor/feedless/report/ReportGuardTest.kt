package org.migor.feedless.report

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.NotFoundException
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.access.AccessDeniedException

/**
 * Der angemeldete Zugriff auf einen Report. Die Links aus den Mails laufen
 * bewusst nicht hier durch, siehe ReportControllerTest.
 */
class ReportGuardTest {

  private val ownerId = UserId()
  private lateinit var reportRepository: ReportRepository
  private lateinit var guard: ReportGuard

  private fun reportOwnedBy(userId: UserId?): Report = Report(
    recipientEmail = "hans@example.com",
    recipientName = "Hans Muster",
    reporterPlugin = PluginExecution(id = "", params = PluginExecutionJson()),
    segmentId = SegmentationId(),
    cronScheduleId = CronSchedule(cronExpression = "").id,
    userId = userId,
  ).also { `when`(reportRepository.findById(it.id)).thenReturn(it) }

  @BeforeEach
  fun setUp() {
    reportRepository = mock(ReportRepository::class.java)
    guard = ReportGuard(reportRepository)
  }

  @Test
  fun `report created by user, it can only be deleted by thee`() =
    runTest(context = RequestContext(userId = ownerId)) {
      val report = reportOwnedBy(ownerId)

      assertThat(guard.requireWrite(report.id)).isEqualTo(report)
    }

  @Test
  fun `another user cannot touch a report that is not theirs`() {
    val report = reportOwnedBy(ownerId)

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = UserId())) {
        guard.requireWrite(report.id)
      }
    }
  }

  /**
   * Ein anonym angelegter Report hat keinen Eigentümer. Liesse der Guard ihn
   * durch, könnte jeder Angemeldete fremde Abos abbestellen.
   */
  @Test
  fun `an anonymous report cannot be changed through a logged in session`() {
    val report = reportOwnedBy(null)

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = UserId())) {
        guard.requireWrite(report.id)
      }
    }
  }

  @Test
  fun `requires a logged in session`() {
    val report = reportOwnedBy(ownerId)

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext()) {
        guard.requireWrite(report.id)
      }
    }
  }

  @Test
  fun `an unknown report is not found`() {
    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = ownerId)) {
        guard.requireWrite(ReportId())
      }
    }
  }
}
