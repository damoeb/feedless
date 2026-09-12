package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.PropertyService
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.document.DocumentRepository
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
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.plugins.EventsReportPlugin
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.template.MailTemplateReportCreated
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.kotlin.argumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.scheduling.support.CronExpression
import org.springframework.security.oauth2.jwt.Jwt
import java.time.LocalDateTime

/**
 * Anlegen und Bestätigen eines Abos.
 *
 * Die Repository- und Nutzer-Guards sind hier echt, nicht gemockt. Mit einem
 * gemockten RepositoryGuard lief der frühere Test "reports can be created by
 * anonymous" durch, obwohl er als Eigentümer lief und ein echter Guard einen
 * anonymen Besucher abgewiesen hätte.
 */
class ReportUseCaseTest {

  private lateinit var documentRepository: DocumentRepository
  private lateinit var propertyService: PropertyService
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var reportUseCase: ReportUseCase
  private lateinit var reportRepository: ReportRepository
  private lateinit var cronScheduleRepository: CronScheduleRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private lateinit var segmentationRepository: SegmentationRepository
  private lateinit var repositoryId: RepositoryId
  private lateinit var segment: SegmentInput
  private lateinit var repository: Repository
  private lateinit var repositoryOwnerId: UserId
  private lateinit var user: User
  private lateinit var userRepository: UserRepository
  private lateinit var templateService: TemplateService
  private lateinit var mailService: MailService
  private val eventsReportPlugin = EventsReportPlugin()
  private lateinit var pluginService: PluginService

  /** Ein Besucher ohne Konto: sein Token trägt eine UserId ohne Zeile in t_user. */
  private val anonymousId = randomUserId()

  @BeforeEach
  fun setUp() = runTest {
    repositoryId = randomRepositoryId()
    reportRepository = mock(ReportRepository::class.java)
    cronScheduleRepository = mock(CronScheduleRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    segmentationRepository = mock(SegmentationRepository::class.java)
    user = mock(User::class.java)
    userRepository = mock(UserRepository::class.java)
    templateService = mock(TemplateService::class.java)
    mailService = mock(MailService::class.java)
    pluginService = PluginService(
      emptyList(),
      emptyList(),
      listOf(eventsReportPlugin),
    )

    documentRepository = mock(DocumentRepository::class.java)
    propertyService = mock(PropertyService::class.java)
    `when`(propertyService.apiGatewayUrl).thenReturn("https://api.test.local")
    jwtTokenIssuer = mock(JwtTokenIssuer::class.java)
    `when`(jwtTokenIssuer.createJwtForReport(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("t")
        .header("alg", "HS256")
        .claim("report_id", "x")
        .build()
    )

    repository = mock(Repository::class.java)
    repositoryOwnerId = randomUserId()
    `when`(repository.ownerId).thenReturn(repositoryOwnerId)
    `when`(repositoryRepository.findById(any(RepositoryId::class.java))).thenReturn(repository)

    // Nur der Eigentümer hat ein Konto; der anonyme Besucher nicht.
    `when`(userRepository.findById(repositoryOwnerId)).thenReturn(user)

    reportUseCase = ReportUseCase(
      reportRepository,
      cronScheduleRepository,
      repositoryRepository,
      segmentationRepository,
      mock(MeterRegistry::class.java),
      RepositoryGuard(repositoryRepository, UserGuard(userRepository)),
      templateService,
      pluginService,
      mailService,
      mock(ReportGuard::class.java),
      documentRepository,
      "no-reply@test.local",
      propertyService,
      userRepository,
      jwtTokenIssuer,
    )

    `when`(segmentationRepository.save(any(Segmentation::class.java))).thenAnswer { it.arguments[0] }
    `when`(reportRepository.save(any(Report::class.java))).thenAnswer { it.arguments[0] }

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
          pluginId = EventsReportPlugin().id(),
          params = PluginExecutionParamsInput()
        )
      ),
      recipient = ReportRecipientInput(
        email = ReportEmailRecipientInput(
          email = "hans@example.com",
          name = "Hans Muster"
        )
      ),
    )

    `when`(templateService.renderTemplate(any2<MailTemplateReportCreated>())).thenReturn("")
  }

  // mockito-kotlin statt ArgumentCaptor.forClass: dessen capture() liefert null
  // an einen Nicht-null-Parameter, und die abgebrochene Verifikation vergiftet
  // Mockitos Zustand für die folgenden Tests.
  private fun savedReport(): Report {
    val captor = argumentCaptor<Report>()
    verify(reportRepository).save(captor.capture())
    return captor.firstValue
  }

  /**
   * Der Weg jedes Abos auf lokale.events: ein Besucher ohne Konto abonniert
   * das öffentliche Veranstaltungs-Repository, dessen Eigentümer er nicht ist.
   */
  @Test
  fun `reports can be created by anonymous if repository is public`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      val report = reportUseCase.createReport(repositoryId, segment)

      assertThat(report).isNotNull
      verify(reportRepository).save(any(Report::class.java))
    }

  @Test
  fun `reports cannot be created by anonymous if repository is private`() {
    `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
        reportUseCase.createReport(repositoryId, segment)
      }
    }
    verify(reportRepository, never()).save(any(Report::class.java))
  }

  @Test
  fun `reports can be created by owner if repository is private`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPrivate)

      val report = reportUseCase.createReport(repositoryId, segment)

      assertThat(report).isNotNull
      verify(reportRepository).save(any(Report::class.java))
    }

  /**
   * Das anonyme Token trägt eine frisch erfundene UserId. Gespeichert verletzt
   * sie den Fremdschlüssel fk_report__to__user.
   */
  @Test
  fun `an anonymous subscription has no owner`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      reportUseCase.createReport(repositoryId, segment)

      assertThat(savedReport().userId).isNull()
    }

  @Test
  fun `a subscription by a known user keeps them as owner`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      reportUseCase.createReport(repositoryId, segment)

      assertThat(savedReport().userId).isEqualTo(repositoryOwnerId)
    }

  /**
   * Genau eine Anfrage, und sie trägt einen Bestätigungslink. Die früheren
   * Rümpfe mit Erinnerungen nach einem Tag, einer Woche und einem Monat sind
   * entfernt - entschieden ist eine einzige Anfrage ohne Erinnerungen.
   */
  @Test
  fun `asks once for authorization when a report is created`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      reportUseCase.createReport(repositoryId, segment)

      verify(mailService).send(any(OutgoingMail::class.java))
      verify(templateService).renderTemplate(argThat<MailTemplateReportCreated> {
        it.params.confirmationLink.contains("/reports/confirm/") &&
          it.params.confirmationLink.contains("token=")
      })
    }

  /**
   * Springs CronExpression verlangt sechs Felder. Der früher gespeicherte
   * Ausdruck "0 8 * * 0" hatte fünf, und nextCronDate warf bei jeder
   * Fortschreibung des Termins.
   */
  @Test
  fun `stores a schedule the cron parser accepts`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      reportUseCase.createReport(repositoryId, segment)

      val captor = argumentCaptor<CronSchedule>()
      verify(cronScheduleRepository).save(captor.capture())
      assertThat(CronExpression.isValidExpression(captor.firstValue.cronExpression)).isTrue()
    }

  private fun unconfirmedReport(authorized: Boolean = false): Report = Report(
    recipientEmail = "hans@example.com",
    recipientName = "Hans Muster",
    reporterPlugin = PluginExecution(id = "", params = PluginExecutionJson()),
    segmentId = SegmentationId(),
    cronScheduleId = CronSchedule(cronExpression = "").id,
    authorized = authorized,
  ).also { `when`(reportRepository.findById(it.id)).thenReturn(it) }

  @Test
  fun `confirming through the mail link authorizes the report`() = runTest {
    val report = unconfirmedReport()

    reportUseCase.confirmReportFromToken(report.id)

    val confirmed = savedReport()
    assertThat(confirmed.authorized).isTrue()
    assertThat(confirmed.authorizedAt).isNotNull()
  }

  @Test
  fun `confirming twice changes nothing`() = runTest {
    val report = unconfirmedReport(authorized = true)

    reportUseCase.confirmReportFromToken(report.id)

    verify(reportRepository, never()).save(any(Report::class.java))
  }

  @Test
  fun `processReportJobs will load pending reports`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      `when`(reportRepository.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(emptyList())

      reportUseCase.processReportJobs()

      verify(reportRepository).findAllPendingBatched(any(LocalDateTime::class.java))
    }
}
