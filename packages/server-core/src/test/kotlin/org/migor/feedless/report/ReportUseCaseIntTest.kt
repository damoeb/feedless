package org.migor.feedless.report

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.Vertical
import org.migor.feedless.any
import org.migor.feedless.attachment.AttachmentRepository
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.HttpService
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.data.jpa.order.OrderDAO
import org.migor.feedless.data.jpa.repository.RepositoryClaimJpaRepository
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.plugins.EventsReportPlugin
import org.migor.feedless.pipeline.plugins.FulltextPlugin
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.source.SourceHarvester
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.toLocalDateTime
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * The tracer bullet against a real database: create, send, unsubscribe.
 */
@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  AppProfiles.properties,
  "database",
  AppProfiles.report,
  AppProfiles.document,
  AppProfiles.repository,
  AppProfiles.user,
  AppProfiles.scrape,
  AppLayer.repository,
  AppLayer.service,
)
@MockitoBean(
  types = [
    ProductRepository::class,
    RepositoryUseCase::class,
    HttpService::class,
    DocumentPipelineJobRepository::class,
    FeatureService::class,
    ProductUseCase::class,
    SourceHarvester::class,
    AttachmentRepository::class,
    GroupUseCase::class,
    UserGuard::class,
    RepositoryClaimJpaRepository::class,
    StatelessAuthService::class,
    PlanConstraintsService::class,
    FulltextPlugin::class,
    ScrapeService::class,
    OrderDAO::class,
  ]
)
@Testcontainers
class ReportUseCaseIntTest {

  private lateinit var repository: Repository

  @Autowired
  private lateinit var reportUseCase: ReportUseCase

  @Autowired
  private lateinit var reportRepository: ReportRepository

  @Autowired
  private lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var documentRepository: DocumentRepository

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @Autowired
  private lateinit var reportRecipientRepository: ReportRecipientRepository

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @MockitoBean
  private lateinit var mailService: MailService

  /**
   * JwtTokenIssuer depends on the session profile, which is inactive here.
   * The report path needs it only to sign the links in the mails.
   */
  @MockitoBean
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  private lateinit var user: User
  private lateinit var group: Group

  private val past = LocalDateTime.now().minusDays(1)
  private val future = LocalDateTime.now().plusDays(1)

  @BeforeEach
  fun setUp() = runTest {
    userRepository.deleteAll()

    whenever(jwtTokenIssuer.createJwtForReport(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("token").header("alg", "HS256").claim("report_id", "x").build()
    )
    whenever(jwtTokenIssuer.createJwtForRecipient(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("token").header("alg", "HS256").claim("recipient_id", "x").build()
    )

    user = User(
      email = "test@test.com",
      lastLogin = LocalDateTime.now(),
    )
    userRepository.save(user)

    group = groupRepository.save(
      Group(
        name = "test-group",
        ownerId = user.id
      )
    )

    repository = createRepository("A", user, group.id)

    assertThat(repositoryRepository.countByGroupId(group.id)).isEqualTo(1)
    assertThat(documentRepository.countByRepositoryId(repository.id)).isEqualTo(4)
  }

  private suspend fun addDocuments(it: Repository) {
    createDocument(
      it,
      title = "past-released",
      status = ReleaseStatus.released,
      publishedAt = past,
      startingAt = past,
      createdAt = past,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
    // A real event: harvested yesterday, happening tomorrow. The document
    // query deliberately filters on publishedAt < now.
    createDocument(
      it,
      // With markup in the title, as it can come from scraped sources.
      title = "future-released <b>bold</b>",
      status = ReleaseStatus.released,
      publishedAt = past,
      startingAt = future,
      createdAt = future,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
    createDocument(
      it,
      title = "past-unreleased",
      status = ReleaseStatus.unreleased,
      publishedAt = past,
      startingAt = past,
      createdAt = past,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
    createDocument(
      it,
      title = "future-unreleased",
      status = ReleaseStatus.unreleased,
      publishedAt = future,
      startingAt = future,
      createdAt = future,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
  }

  /**
   * Public, like the events repository of lokale.events: otherwise the
   * anonymous path couldn't be verified.
   */
  private suspend fun createRepository(suffix: String, user: User, groupId: GroupId): Repository {
    val repository = Repository(
      title = "title $suffix",
      description = "description $suffix",
      sourcesSyncCron = "",
      shareKey = "1234",
      product = Vertical.rssProxy,
      ownerId = user.id,
      groupId = groupId,
      visibility = EntityVisibility.isPublic,
      lastUpdatedAt = LocalDateTime.now().minusDays(2),
      retentionMaxAgeDaysReferenceField = MaxAgeDaysDateField.createdAt,
    )

    return repositoryRepository.save(repository).also { addDocuments(it) }
  }

  private suspend fun createDocument(
    repository: Repository,
    title: String,
    status: ReleaseStatus,
    publishedAt: LocalDateTime,
    startingAt: LocalDateTime,
    createdAt: LocalDateTime,
    latlon: Point
  ) {
    val d = Document(
      url = "http://localhost:8080",
      title = title,
      text = "",
      repositoryId = repository.id,
      status = status,
      publishedAt = publishedAt,
      contentHash = CryptUtil.sha1(newCorrId()),
      startingAt = startingAt,
      createdAt = createdAt,
      latLon = latlon
    )

    documentRepository.save(d)
  }

  @AfterEach
  fun tearDown() {
    userRepository.deleteAll()
  }

  @Test
  fun `when creating a report, user receives a mail`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      createReport()

      verify(mailService).send(any(OutgoingMail::class.java))
    }

  /**
   * The actual tracer bullet: with no confirmation step, the scheduled run
   * sends next week's events - only released ones, only future ones.
   */
  @Test
  fun `a new report is sent with the events of the coming week`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      createReport()
      reset(mailService)

      reportUseCase.processReportJobs()

      val captor = argumentCaptor<OutgoingMail>()
      verify(mailService).send(captor.capture())
      assertThat(captor.firstValue.to).containsExactly("email@somewhere")
      assertThat(captor.firstValue.htmlContent)
        .contains("future-released")
        .doesNotContain("past-released")
        .doesNotContain("future-unreleased")
        // Every report mail needs a working unsubscribe link.
        .contains("/reports/delete/")
        .contains("/reports/abuse/")
        // Scraped titles land escaped in the mail, not as HTML.
        .contains("future-released &lt;b&gt;bold&lt;/b&gt;")
        .doesNotContain("<b>bold</b>")
        .doesNotContain("href=\"\"")
    }

  @Test
  fun `a cancelled report is not sent`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      val report = createReport()
      reportUseCase.deleteReportFromToken(report.id)
      reset(mailService)

      reportUseCase.processReportJobs()

      verify(mailService, never()).send(any(OutgoingMail::class.java))
    }

  /**
   * Against the real database, because two bugs sat here together: the
   * RepositoryGuard demanded ownership, and the anonymous token's invented
   * UserId violated the foreign key fk_report__to__user.
   */
  @Test
  fun `an anonymous visitor can subscribe to a public repository`() =
    runTest(context = RequestContext(userId = UserId())) {
      val report = createReport()

      assertThat(reportRepository.findById(report.id)).isNotNull
      assertThat(reportRepository.findById(report.id)!!.userId).isNull()
    }

  @Test
  fun `reporting abuse stops every report to the address, whatever its case`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      val report = createReport()
      // a row stored before addresses were normalized
      jdbcTemplate.update("UPDATE t_report SET recipient_email = ? WHERE id = ?", " EMAIL@Somewhere ", report.id.uuid)
      val recipient = reportRecipientRepository.findByEmail("email@somewhere")!!

      reportUseCase.reportAbuse(recipient.id)
      reset(mailService)
      reportUseCase.processReportJobs()

      verify(mailService, never()).send(any(OutgoingMail::class.java))
      assertThat(reportRepository.findById(report.id)!!.disabled).isTrue()
      assertThat(reportRecipientRepository.findById(recipient.id)!!.optInRequired).isTrue()
    }

  @Test
  fun `after an abuse report a new subscription waits for its confirmation`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      createReport()
      reportUseCase.reportAbuse(reportRecipientRepository.findByEmail("email@somewhere")!!.id)
      reset(mailService)

      val pending = createReport()

      assertThat(reportRepository.findById(pending.id)!!.authorized).isFalse()
      val captor = argumentCaptor<OutgoingMail>()
      verify(mailService).send(captor.capture())
      assertThat(captor.firstValue.subject).isEqualTo("Bitte bestätige dein Abo")
      assertThat(captor.firstValue.htmlContent).contains("/reports/confirm/").doesNotContain("/reports/abuse/")

      reset(mailService)
      reportUseCase.processReportJobs()
      verify(mailService, never()).send(any(OutgoingMail::class.java))

      reportUseCase.confirmReportFromToken(pending.id)
      reportUseCase.processReportJobs()
      verify(mailService).send(any(OutgoingMail::class.java))
    }

  private suspend fun createReport(): Report =
    reportUseCase.createReport(
      repository.id,
      SegmentCreate(
        recipientEmail = "email@somewhere",
        recipientName = "RecipientName",
        startingAt = 0L.toLocalDateTime(),
        interval = ChronoUnit.WEEKS,
        reporterPluginId = EventsReportPlugin().id(),
      )
    )
}
