package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.NotFoundException
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.AppConfig
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.document.DocumentRepository
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
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.template.MailTemplateReportConfirmRequest
import org.migor.feedless.template.MailTemplateReportCreated
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.util.toLocalDateTime
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
import java.time.temporal.ChronoUnit

/**
 * Anlegen eines Abos.
 *
 * Die Repository- und Nutzer-Guards sind hier echt, nicht gemockt. Mit einem
 * gemockten RepositoryGuard lief der frühere Test "reports can be created by
 * anonymous" durch, obwohl er als Eigentümer lief und ein echter Guard einen
 * anonymen Besucher abgewiesen hätte.
 */
class ReportUseCaseTest {

  private lateinit var documentRepository: DocumentRepository
  private lateinit var appConfig: AppConfig
  private lateinit var tokenIssuer: TokenIssuer
  private lateinit var reportUseCase: ReportUseCase
  private lateinit var reportRepository: ReportRepository
  private lateinit var cronScheduleRepository: CronScheduleRepository
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
  private lateinit var reportRecipientRepository: ReportRecipientRepository
  private val reportPluginId = "org_feedless_event_report"
  private lateinit var pipelinePlugins: PipelinePlugins

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
    pipelinePlugins = mock(PipelinePlugins::class.java)
    `when`(pipelinePlugins.resolveById(any(String::class.java), eq(ReportPlugin::class)))
      .thenReturn(mock(ReportPlugin::class.java))

    documentRepository = mock(DocumentRepository::class.java)
    appConfig = mock(AppConfig::class.java)
    `when`(appConfig.apiGatewayUrl).thenReturn("https://api.test.local")
    tokenIssuer = mock(TokenIssuer::class.java)
    `when`(tokenIssuer.createJwtForReport(anyString(), anyLong())).thenReturn(
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

    reportRecipientRepository = mock(ReportRecipientRepository::class.java)
    `when`(reportRecipientRepository.save(any(ReportRecipient::class.java))).thenAnswer { it.arguments[0] }
    `when`(tokenIssuer.createJwtForRecipient(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("r").header("alg", "HS256").claim("recipient_id", "x").build()
    )

    reportUseCase = newUseCase("opt-out")

    `when`(segmentationRepository.save(any(Segmentation::class.java))).thenAnswer { it.arguments[0] }
    `when`(reportRepository.save(any(Report::class.java))).thenAnswer { it.arguments[0] }

    segment = SegmentCreate(
      recipientEmail = "hans@example.com",
      recipientName = "Hans Muster",
      startingAt = 0L.toLocalDateTime(),
      interval = ChronoUnit.WEEKS,
      reporterPluginId = reportPluginId,
    )

    `when`(templateService.renderTemplate(any2<MailTemplateReportCreated>())).thenReturn("")
  }

  private fun newUseCase(subscriptionMode: String) = ReportUseCase(
    reportRepository,
    cronScheduleRepository,
    repositoryRepository,
    segmentationRepository,
    mock(MeterRegistry::class.java),
    RepositoryGuard(
      repositoryRepository,
      UserGuard(userRepository),
      mock(UserGroupAssignmentRepository::class.java),
    ),
    templateService,
    pipelinePlugins,
    mailService,
    mock(ReportGuard::class.java),
    documentRepository,
    "no-reply@test.local",
    appConfig,
    userRepository,
    tokenIssuer,
    reportRecipientRepository,
    subscriptionMode,
  )

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

    assertThatExceptionOfType(NotFoundException::class.java).isThrownBy {
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

  @Test
  fun `a new report is active at once and its confirmation mail can cancel it`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      reportUseCase.createReport(repositoryId, segment)

      assertThat(savedReport().authorized).isTrue()
      verify(mailService).send(any(OutgoingMail::class.java))
      verify(templateService).renderTemplate(argThat<MailTemplateReportCreated> {
        it.params.deactivationLink.contains("/reports/delete/") &&
          it.params.deactivationLink.contains("token=") &&
          it.params.abuseLink.contains("/reports/abuse/")
      })
    }

  @Test
  fun `stores the address normalized and resolves its recipient by it`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      reportUseCase.createReport(repositoryId, segment.copy(recipientEmail = "  Hans@Example.COM "))

      assertThat(savedReport().recipientEmail).isEqualTo("hans@example.com")
      verify(reportRecipientRepository).findByEmail("hans@example.com")
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

  @Test
  fun `processReportJobs will load pending reports`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = repositoryOwnerId)) {
      `when`(reportRepository.findAllPendingBatched(any(LocalDateTime::class.java))).thenReturn(emptyList())

      reportUseCase.processReportJobs()

      verify(reportRepository).findAllPendingBatched(any(LocalDateTime::class.java))
    }

  private fun flagged(email: String = "hans@example.com") {
    `when`(reportRecipientRepository.findByEmail(email))
      .thenReturn(ReportRecipient(email = email, optInRequired = true))
  }

  @Test
  fun `a flagged address gets an inactive report and a confirmation request`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)
      flagged()

      reportUseCase.createReport(repositoryId, segment)

      assertThat(savedReport().authorized).isFalse()
      verify(templateService).renderTemplate(argThat<MailTemplateReportConfirmRequest> {
        it.params.confirmationLink.contains("/reports/confirm/")
      })
      verify(templateService, never()).renderTemplate(any(MailTemplateReportCreated::class.java))
    }

  @Test
  fun `in opt-in mode every new report waits for confirmation`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)
      reportUseCase = newUseCase("opt-in")

      reportUseCase.createReport(repositoryId, segment)

      assertThat(savedReport().authorized).isFalse()
    }

  @Test
  fun `rejects an unknown subscription mode`() {
    assertThatThrownBy { newUseCase("sometimes") }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `reporting abuse flags the address and stops its reports`() = runTest {
    val recipient = ReportRecipient(email = "hans@example.com")
    `when`(reportRecipientRepository.findById(recipient.id)).thenReturn(recipient)

    reportUseCase.reportAbuse(recipient.id)

    verify(reportRecipientRepository).save(argThat<ReportRecipient> { it.id == recipient.id && it.optInRequired })
    verify(reportRepository).disableAllByRecipientEmail(eq("hans@example.com"), any(LocalDateTime::class.java))
  }

  @Test
  fun `reporting abuse again keeps the flag and still stops reports`() = runTest {
    val recipient = ReportRecipient(email = "hans@example.com", optInRequired = true)
    `when`(reportRecipientRepository.findById(recipient.id)).thenReturn(recipient)

    reportUseCase.reportAbuse(recipient.id)

    verify(reportRecipientRepository, never()).save(any(ReportRecipient::class.java))
    verify(reportRepository).disableAllByRecipientEmail(eq("hans@example.com"), any(LocalDateTime::class.java))
  }

  private fun storedReport(authorized: Boolean, disabled: Boolean = false): Report = Report(
    recipientEmail = "hans@example.com",
    recipientName = "Hans Muster",
    reporterPlugin = PluginExecution(id = reportPluginId, params = PluginExecutionJson()),
    segmentId = SegmentationId(),
    cronScheduleId = CronSchedule(cronExpression = WEEKLY_REPORT_CRON).id,
    authorized = authorized,
    disabled = disabled,
  ).also { `when`(reportRepository.findById(it.id)).thenReturn(it) }

  @Test
  fun `confirming activates a pending report`() = runTest {
    val report = storedReport(authorized = false)

    reportUseCase.confirmReportFromToken(report.id)

    assertThat(savedReport().authorized).isTrue()
  }

  @Test
  fun `confirming does not revive a report stopped by an abuse report`() = runTest {
    val report = storedReport(authorized = false, disabled = true)

    reportUseCase.confirmReportFromToken(report.id)

    verify(reportRepository, never()).save(any(Report::class.java))
  }
}
