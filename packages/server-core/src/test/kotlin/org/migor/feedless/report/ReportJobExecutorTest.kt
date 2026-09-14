package org.migor.feedless.report

import com.google.gson.Gson
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.any
import org.migor.feedless.common.PropertyService
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.mail.MailService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.pipeline.plugins.EventsReportPluginParams
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.argumentCaptor
import org.springframework.security.oauth2.jwt.Jwt
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verifyBlocking
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/** What the scheduled run does with due reports. */
class ReportJobExecutorTest {

  /** A six-field expression, as Spring's CronExpression requires. */
  private val weeklyCron = "0 0 8 * * FRI"
  private val ownerId = UserId()

  private lateinit var reportRepository: ReportRepository
  private lateinit var cronScheduleRepository: CronScheduleRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var repository: Repository
  private lateinit var plugin: RecordingReportPlugin
  private lateinit var reportUseCase: ReportUseCase

  /** Records who a report was sent to, instead of actually sending mail. */
  class RecordingReportPlugin : ReportPlugin<Unit> {
    val recipients = mutableListOf<String>()
    var failWith: Exception? = null

    override fun id(): String = FeedlessPlugins.org_feedless_event_report.name
    override fun name(): String = "recording"
    override fun listed() = false

    override suspend fun report(
      documents: List<Document>,
      repository: Repository,
      params: Unit,
      logCollector: LogCollector,
    ) = Unit

    override suspend fun report(
      documents: List<Document>,
      repository: Repository,
      params: PluginExecutionJson,
      logCollector: LogCollector,
    ) {
      failWith?.let { throw it }
      recipients += Gson().fromJson(params.paramsJsonString, EventsReportPluginParams::class.java).to
    }
  }

  @BeforeEach
  fun setUp() {
    reportRepository = mock(ReportRepository::class.java)
    cronScheduleRepository = mock(CronScheduleRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    plugin = RecordingReportPlugin()

    repository = mock(Repository::class.java)
    `when`(repository.ownerId).thenReturn(ownerId)
    `when`(repository.title).thenReturn("Veranstaltungen")
    `when`(repository.product).thenReturn(Vertical.upcoming)
    `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)
    `when`(repositoryRepository.findById(any(RepositoryId::class.java))).thenReturn(repository)

    val userRepository = mock(UserRepository::class.java)
    val jwtTokenIssuer = mock(JwtTokenIssuer::class.java)
    `when`(jwtTokenIssuer.createJwtForReport(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("token").header("alg", "HS256").claim("report_id", "x").build()
    )
    val reportRecipientRepository = mock(ReportRecipientRepository::class.java)
    `when`(reportRecipientRepository.save(any(ReportRecipient::class.java))).thenAnswer { it.arguments[0] }
    `when`(jwtTokenIssuer.createJwtForRecipient(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("token").header("alg", "HS256").claim("recipient_id", "x").build()
    )
    reportUseCase = ReportUseCase(
      reportRepository,
      cronScheduleRepository,
      repositoryRepository,
      mock(SegmentationRepository::class.java),
      mock(MeterRegistry::class.java),
      mock(RepositoryGuard::class.java),
      mock(TemplateService::class.java),
      PluginService(emptyList(), emptyList(), listOf(plugin)),
      mock(MailService::class.java),
      mock(ReportGuard::class.java),
      mock(DocumentRepository::class.java),
      "no-reply@test.local",
      mock(PropertyService::class.java),
      userRepository,
      jwtTokenIssuer,
      reportRecipientRepository,
      "opt-out",
    )
  }

  private fun pendingReport(
    email: String,
    cron: String = weeklyCron,
    userId: UserId? = null,
  ): Report {
    val cronSchedule = CronSchedule(
      cronExpression = cron,
      scheduledNextAt = LocalDateTime.now().minusMinutes(1),
    )
    val segment = Segmentation(
      size = 200,
      timeSegmentStartingAt = LocalDateTime.now(),
      timeInterval = ChronoUnit.WEEKS,
      repositoryId = RepositoryId(),
    )
    return Report(
      recipientEmail = email,
      recipientName = "Hans Muster",
      reporterPlugin = PluginExecution(
        id = FeedlessPlugins.org_feedless_event_report.name,
        params = PluginExecutionJson(),
      ),
      segmentId = segment.id,
      segment = segment,
      cronScheduleId = cronSchedule.id,
      cronSchedule = cronSchedule,
      authorized = true,
      userId = userId,
    )
  }

  private fun pending(vararg reports: Report) {
    `when`(reportRepository.findAllPendingBatched(any(LocalDateTime::class.java)))
      .thenReturn(reports.toList())
  }

  private fun savedSchedules(): List<CronSchedule> {
    val captor = argumentCaptor<CronSchedule>()
    verify(cronScheduleRepository, atLeastOnce()).save(captor.capture())
    return captor.allValues
  }

  @Test
  fun `delegates the scheduled run to processReportJobs`() {
    val useCase = mock(ReportUseCase::class.java)

    ReportJobExecutor(useCase).sendScheduledReports()

    verifyBlocking(useCase) { processReportJobs() }
  }

  @Test
  fun `authorized reports will be shipped`() = runTest {
    pending(pendingReport("hans@example.com"))

    reportUseCase.processReportJobs()

    assertThat(plugin.recipients).containsExactly("hans@example.com")
  }

  /**
   * Previously the schedule advanced only on failure. A successfully
   * delivered report stayed due and went out again 60 seconds later.
   */
  @Test
  fun `advances the schedule after a successful send`() = runTest {
    val report = pendingReport("hans@example.com")
    pending(report)

    reportUseCase.processReportJobs()

    val next = savedSchedules().single().scheduledNextAt!!
    assertThat(next).isAfter(report.cronSchedule!!.scheduledNextAt)
    assertThat(next.dayOfWeek).isEqualTo(DayOfWeek.FRIDAY)
    assertThat(next.hour).isEqualTo(8)
  }

  @Test
  fun `advances the schedule after a failed send too`() = runTest {
    plugin.failWith = RuntimeException("mail server down")
    pending(pendingReport("hans@example.com"))

    reportUseCase.processReportJobs()

    assertThat(savedSchedules()).hasSize(1)
  }

  /**
   * Legacy rows still carry the previously stored empty string as their cron
   * expression. If advancing it throws, it must not take down the rest of
   * this run's reports.
   */
  @Test
  fun `keeps processing the batch when one report has a broken schedule`() = runTest {
    pending(
      pendingReport("legacy@example.com", cron = ""),
      pendingReport("hans@example.com"),
    )

    reportUseCase.processReportJobs()

    assertThat(plugin.recipients).containsExactly("legacy@example.com", "hans@example.com")
    assertThat(savedSchedules()).hasSize(2)
  }

  /**
   * A subscription to a repository that later turns private must not keep
   * sending its content to a foreign address. Only the owner receives
   * reports from a private repository.
   */
  @Test
  fun `only processes reports that have permissions for the repository`() = runTest {
    `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)
    pending(
      pendingReport("stranger@example.com", userId = null),
      pendingReport("owner@example.com", userId = ownerId),
    )

    reportUseCase.processReportJobs()

    assertThat(plugin.recipients).containsExactly("owner@example.com")
  }
}
