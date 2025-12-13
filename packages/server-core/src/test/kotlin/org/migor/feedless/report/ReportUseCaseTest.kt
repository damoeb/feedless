package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.any
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
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class ReportUseCaseTest {

  private lateinit var reportUseCase: ReportUseCase
  private lateinit var reportRepository: ReportRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var segmentationRepository: SegmentationRepository
  private lateinit var repositoryId: RepositoryId
  private lateinit var segment: SegmentInput
  private lateinit var repository: Repository
  private lateinit var repositoryOwnerId: UserId
  private lateinit var user: User
  private lateinit var userRepository: UserRepository

  @BeforeEach
  fun setUp() = runTest {
    repositoryId = randomRepositoryId()
    reportRepository = mock(ReportRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    segmentationRepository = mock(SegmentationRepository::class.java)
    user = mock(User::class.java)
    userRepository = mock(UserRepository::class.java)

    reportUseCase = ReportUseCase(
      reportRepository,
      repositoryRepository,
      userRepository,
      segmentationRepository,
      mock(MeterRegistry::class.java),
    )

    `when`(segmentationRepository.save(any(Segmentation::class.java))).thenAnswer { it.arguments[0] }
    `when`(reportRepository.save(any(Report::class.java))).thenAnswer { it.arguments[0] }

    repository = mock(Repository::class.java)
    repositoryOwnerId = randomUserId()
    `when`(repository.ownerId).thenReturn(repositoryOwnerId)
    `when`(repositoryRepository.findById(any(RepositoryId::class.java))).thenReturn(repository)

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
  fun `reports can be created by anonymous if repository is public`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      // given
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)
      `when`(userRepository.findByEmail(any(String::class.java))).thenReturn(null)

      // when
      val report = reportUseCase.createReport(repositoryId, segment)

      // then
      assertThat(report).isNotNull
      verify(reportRepository).save(any(Report::class.java))
    }

  @Test
  @Disabled
  fun `reports cannot be created by anonymous if repository is private`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
        `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)
        `when`(userRepository.findByEmail(any(String::class.java))).thenReturn(null)
        reportUseCase.createReport(repositoryId, segment)
      }
    }
  }

  @Test
  fun `reports can be created by owner if repository is private`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)
      `when`(userRepository.findByEmail(any(String::class.java))).thenReturn(user)

      // when
      val report = reportUseCase.createReport(repositoryId, segment)

      // then
      assertThat(report).isNotNull
      verify(reportRepository).save(any(Report::class.java))
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
  @Disabled("rethink that idea")
  fun `if email belongs to user, he will be asked to login`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest {
        `when`(userRepository.findByEmail(any(String::class.java))).thenReturn(user)
        reportUseCase.createReport(repositoryId, segment)
      }
    }
  }
}
