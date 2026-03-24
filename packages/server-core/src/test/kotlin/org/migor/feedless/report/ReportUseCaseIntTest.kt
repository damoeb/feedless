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
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.Vertical
import org.migor.feedless.any
import org.migor.feedless.attachment.AttachmentRepository
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.data.jpa.order.OrderDAO
import org.migor.feedless.data.jpa.repository.RepositoryClaimJpaRepository
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ReportEmailRecipientInput
import org.migor.feedless.generated.types.ReportRecipientInput
import org.migor.feedless.generated.types.ScheduledSegmentInput
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.generated.types.SegmentRecordsWhereInput
import org.migor.feedless.generated.types.SegmentReportInput
import org.migor.feedless.generated.types.TimeSegmentInput
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.plugins.FulltextPlugin
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserRepository
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.newCorrId
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
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
    PropertyService::class,
    RepositoryHarvester::class,
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
  private lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var documentRepository: DocumentRepository

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @MockitoBean
  private lateinit var mailService: MailService
  private lateinit var user: User
  private lateinit var group: Group

  private val past = LocalDateTime.now().minusDays(1)
  private val future = LocalDateTime.now().plusDays(1)

  @BeforeEach
  fun setUp() = runTest {
    // Clean up before each test
    userRepository.deleteAll()

    user = User(
      email = "test@test.com",
      lastLogin = LocalDateTime.now(),
    )
    userRepository.save(user)

    // Create a group for the user
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
    createDocument(
      it,
      title = "future-released",
      status = ReleaseStatus.released,
      publishedAt = future,
      startingAt = future,
      createdAt = future,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
    createDocument(
      it,
      title = "3",
      status = ReleaseStatus.unreleased,
      publishedAt = past,
      startingAt = past,
      createdAt = past,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
    createDocument(
      it,
      title = "4",
      status = ReleaseStatus.unreleased,
      publishedAt = future,
      startingAt = future,
      createdAt = future,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
  }

  private suspend fun createRepository(suffix: String, user: User, groupId: GroupId): Repository {
    val repository = Repository(
      title = "title $suffix",
      description = "description $suffix",
      sourcesSyncCron = "",
      shareKey = "1234",
      product = Vertical.rssProxy,
      ownerId = user.id,
      groupId = groupId,
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

  @Test
  fun `given a report exists, processReportJobs will send a report`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      createReport()
      reset(mailService)

      reportUseCase.processReportJobs()

      verify(mailService).send(any(OutgoingMail::class.java))
    }

  private suspend fun createReport() {
    reportUseCase.createReport(
      repository.id,
      SegmentInput(
        `when` = TimeSegmentInput(
          scheduled = ScheduledSegmentInput(
            interval = IntervalUnit.WEEK,
            startingAt = 0
          ),
        ),
        what = SegmentRecordsWhereInput(),
        report = SegmentReportInput(
          plugin = PluginExecutionInput(
            pluginId = FeedlessPlugins.org_feedless_event_report.name,
            params = PluginExecutionParamsInput(),
          )
        ),
        recipient = ReportRecipientInput(
          email = ReportEmailRecipientInput(
            email = "email@somewhere",
            name = "RecipientName"
          )
        ),
      )
    )
  }

}
