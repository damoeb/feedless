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
import org.migor.feedless.any2
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.eq
import org.migor.feedless.group.GroupId
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.PipelinePlugins
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.template.MailTemplateReportCreated
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.util.toLocalDateTime
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ReportUseCaseTest {

  private lateinit var reportUseCase: ReportUseCase
  private lateinit var reportRepository: ReportRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var segmentationRepository: SegmentationRepository
  private lateinit var repositoryId: RepositoryId
  private lateinit var segment: SegmentCreate
  private lateinit var repository: Repository
  private lateinit var repositoryOwnerId: UserId
  private lateinit var user: User
  private lateinit var userRepository: UserRepository
  private lateinit var templateService: TemplateService
  private lateinit var mailService: MailService
  private val reportPluginId = "org_feedless_event_report"
  private lateinit var pipelinePlugins: PipelinePlugins

  @BeforeEach
  fun setUp() = runTest {
    repositoryId = randomRepositoryId()
    reportRepository = mock(ReportRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    segmentationRepository = mock(SegmentationRepository::class.java)
    user = mock(User::class.java)
    userRepository = mock(UserRepository::class.java)
    templateService = mock(TemplateService::class.java)
    mailService = mock(MailService::class.java)
    pipelinePlugins = mock(PipelinePlugins::class.java)
    `when`(pipelinePlugins.resolveById(any(String::class.java), eq(ReportPlugin::class)))
      .thenReturn(mock(ReportPlugin::class.java))

    reportUseCase = ReportUseCase(
      reportRepository,
      mock(CronScheduleRepository::class.java),
      repositoryRepository,
      segmentationRepository,
      mock(MeterRegistry::class.java),
      mock(RepositoryGuard::class.java),
      templateService,
      pipelinePlugins,
      mailService,
      mock(ReportGuard::class.java),
    )

    `when`(segmentationRepository.save(any(Segmentation::class.java))).thenAnswer { it.arguments[0] }
    `when`(reportRepository.save(any(Report::class.java))).thenAnswer { it.arguments[0] }

    repository = mock(Repository::class.java)
    repositoryOwnerId = randomUserId()
    `when`(repository.ownerId).thenReturn(repositoryOwnerId)
    `when`(repositoryRepository.findById(any(RepositoryId::class.java))).thenReturn(repository)

    segment = SegmentCreate(
      recipientEmail = "",
      recipientName = "",
      startingAt = 0L.toLocalDateTime(),
      interval = ChronoUnit.WEEKS,
      reporterPluginId = reportPluginId,
    )

    `when`(templateService.renderTemplate(any2<MailTemplateReportCreated>())).thenReturn("")

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
  fun `report created by user, user will receive a confirmation mail`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)
      `when`(userRepository.findByEmail(any(String::class.java))).thenReturn(user)

      // when
      val report = reportUseCase.createReport(repositoryId, segment)

      // then
      assertThat(report).isNotNull
      verify(mailService).send(any(OutgoingMail::class.java))
    }

  @Test
  fun `processReportJobs will load pending reports`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      `when`(reportRepository.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(emptyList())

      reportUseCase.processReportJobs()

      verify(reportRepository).findAllPendingBatched(any(LocalDateTime::class.java))
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
