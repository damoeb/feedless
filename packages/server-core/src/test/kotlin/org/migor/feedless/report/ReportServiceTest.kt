package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.any
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.eq
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ReportEmailRecipientInput
import org.migor.feedless.generated.types.ReportRecipientInput
import org.migor.feedless.generated.types.ScheduledSegmentInput
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.generated.types.SegmentRecordsWhereInput
import org.migor.feedless.generated.types.SegmentReportInput
import org.migor.feedless.generated.types.StringFilterInput
import org.migor.feedless.generated.types.TimeSegmentInput
import org.migor.feedless.data.jpa.report.ReportDAO
import org.migor.feedless.data.jpa.report.ReportEntity
import org.migor.feedless.data.jpa.report.SegmentationEntity
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import java.util.*

class ReportServiceTest {

  private lateinit var reportService: ReportService
  private lateinit var reportDAO: ReportDAO
  private lateinit var repositoryService: RepositoryService
  private lateinit var userService: UserService
  private lateinit var segmentationService: SegmentationService
  private lateinit var context: ApplicationContext
  private lateinit var repositoryId: RepositoryId
  private lateinit var segment: SegmentInput
  private lateinit var repository: RepositoryEntity
  private lateinit var repositoryOwnerId: UserId
  private lateinit var user: UserEntity

  @BeforeEach
  fun setUp() = runTest {
    repositoryId = randomRepositoryId()
    reportDAO = mock(ReportDAO::class.java)
    repositoryService = mock(RepositoryService::class.java)
    userService = mock(UserService::class.java)
    segmentationService = mock(SegmentationService::class.java)
    context = mock(ApplicationContext::class.java)
    user = mock(UserEntity::class.java)

    reportService = ReportService(
      reportDAO,
      repositoryService,
      userService,
      segmentationService,
      mock(MeterRegistry::class.java),
      context
    )

    `when`(segmentationService.saveSegmentation(any(SegmentationEntity::class.java))).thenAnswer { it.arguments[0] }
    `when`(reportDAO.save(any(ReportEntity::class.java))).thenAnswer { it.arguments[0] }
    `when`(context.getBean(eq(ReportService::class.java))).thenReturn(reportService)

    repository = mock(RepositoryEntity::class.java)
    repositoryOwnerId = randomUserId()
    `when`(repository.ownerId).thenReturn(repositoryOwnerId.value)
    `when`(repositoryService.findById(any(RepositoryId::class.java))).thenReturn(Optional.of(repository))

    segment = SegmentInput(
      `when` = TimeSegmentInput(
        ScheduledSegmentInput(
          interval = IntervalUnit.WEEK,
          startingAt = 0
        )
      ),
      what = SegmentRecordsWhereInput(tags = StringFilterInput()),
      report = SegmentReportInput(
        plugin = PluginExecutionInput(
          pluginId = "",
          params = PluginExecutionParamsInput()
        )
      ),
      recipient = ReportRecipientInput(
        email = ReportEmailRecipientInput(
          email = "",
          name = ""
        )
      ),
    )

  }

  @Test
  fun `reports can be created by anonymous if repository is public`() = runTest {
    // given
    `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)
    `when`(userService.findByEmail(any(String::class.java))).thenReturn(null)

    // when
    val report = reportService.createReport(repositoryId, segment, null)

    // then
    assertThat(report).isNotNull
    verify(reportDAO).save(any(ReportEntity::class.java))
  }

  @Test
  @Disabled
  fun `reports cannot be created by anonymous if repository is private`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)
        `when`(userService.findByEmail(any(String::class.java))).thenReturn(null)
        reportService.createReport(repositoryId, segment, null)
      }
    }
  }

  @Test
  fun `reports can be created by owner if repository is private`() = runTest {
    `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)
    `when`(userService.findByEmail(any(String::class.java))).thenReturn(user)

    // when
    val report = reportService.createReport(repositoryId, segment, repositoryOwnerId)

    // then
    assertThat(report).isNotNull
    verify(reportDAO).save(any(ReportEntity::class.java))
  }

  @Test
  @Disabled
  fun `report can be deleted by anonymous if created by anonymous`() {
    // todo test
  }

  @Test
  @Disabled
  fun `report created by user, it can only be deleted by thee`() {
    // todo test
  }

  @Test
  @Disabled
  fun `if email is unknown, auth mail is sent`() {
    // todo test
  }

  @Test
  @Disabled
  fun `after sending auth mail there will be an cooldown for this email, where no new reports can be sent`() {
    // todo test
  }

  @Test
  fun `if email belongs to user, he will be asked to login`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        `when`(userService.findByEmail(any(String::class.java))).thenReturn(user)
        reportService.createReport(repositoryId, segment, null)
      }
    }
  }
}
