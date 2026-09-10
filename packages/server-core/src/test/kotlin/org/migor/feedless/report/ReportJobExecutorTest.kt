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

/**
 * Was der geplante Lauf mit fälligen Reports macht.
 *
 * Die früheren Rümpfe mit drei Erinnerungen nach einem Tag, einer Woche und
 * einem Monat sind entfernt: entschieden ist genau eine Bestätigungsanfrage
 * ohne Erinnerungen. Diese Anfrage prüft ReportUseCaseTest, dass unbestätigte
 * Reports nicht verschickt werden, ReportUseCaseIntTest gegen die Datenbank.
 */
class ReportJobExecutorTest {

  /** Ein sechsteiliger Ausdruck, wie Springs CronExpression ihn verlangt. */
  private val weeklyCron = "0 0 8 * * FRI"
  private val ownerId = UserId()

  private lateinit var reportRepository: ReportRepository
  private lateinit var cronScheduleRepository: CronScheduleRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var repository: Repository
  private lateinit var plugin: RecordingReportPlugin
  private lateinit var reportUseCase: ReportUseCase

  /** Zeichnet auf, an wen verschickt wurde, statt Mails zu senden. */
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
   * Vorher wurde der Termin nur im Fehlerfall fortgeschrieben. Ein erfolgreich
   * zugestellter Report blieb fällig und ging 60 Sekunden später erneut raus.
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
   * Bestandszeilen tragen noch den früher gespeicherten Leerstring als
   * Cron-Ausdruck. Wirft dessen Fortschreibung, darf das die übrigen Reports
   * des Laufs nicht mitreissen.
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
   * Ein Abo auf ein Repository, das später privat wird, darf nicht weiter
   * dessen Inhalte an eine fremde Adresse schicken. Nur der Eigentümer bekommt
   * Reports aus einem privaten Repository.
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
