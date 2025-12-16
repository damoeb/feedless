package org.migor.feedless.document

import jakarta.transaction.Transactional
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
import org.migor.feedless.any2
import org.migor.feedless.attachment.AttachmentRepository
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.data.jpa.repository.RepositoryClaimJpaRepository
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipelineJob.DocumentPipelineJobRepository
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.repository.toPageableRequest
import org.migor.feedless.session.RequestContext
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.newCorrId
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
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
  AppProfiles.document,
  AppProfiles.repository,
  AppProfiles.user,
  AppLayer.repository,
  AppLayer.service,
)
@MockitoBean(
  types = [
    ProductRepository::class,
    RepositoryUseCase::class,
    HttpService::class,
    DocumentPipelineJobRepository::class,
    PluginService::class,
    FeatureService::class,
    ProductUseCase::class,
    PropertyService::class,
    RepositoryHarvester::class,
    AttachmentRepository::class,
    GroupUseCase::class,
    UserGuard::class,
    RepositoryClaimJpaRepository::class,
    StatelessAuthService::class,
  ]
)
@Testcontainers
class DocumentIntTest {

  lateinit var repository: Repository

  @Autowired
  lateinit var documentUseCase: DocumentUseCase

  @Autowired
  lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  lateinit var userRepository: UserRepository

  @Autowired
  lateinit var documentRepository: DocumentRepository

  @Autowired
  lateinit var groupRepository: GroupRepository

  @MockitoBean
  lateinit var planConstraintsService: PlanConstraintsService

  private val past = LocalDateTime.now().minusDays(1)
  private val future = LocalDateTime.now().plusDays(1)

  @BeforeEach
  fun setUp() = runTest {
    // Clean up before each test
    userRepository.deleteAll()

    // todo creat user
    val user = User(
      email = "test@test.com",
      lastLogin = LocalDateTime.now(),
    )
    userRepository.save(user)

    // Create a group for the user
    val group = groupRepository.save(
      Group(
        name = "test-group",
        ownerId = user.id
      )
    )

    repository = createRepository("A", user, group.id)
    createRepository("B", user, group.id)

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
  fun `given where is null, findAll filters repoId and status`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = UserId())) {
      val documents = documentUseCase.findAllByRepositoryId(
        repositoryId = repository.id,
        status = ReleaseStatus.released,
        pageable = PageRequest.of(0, 10).toPageableRequest(),
      )
      assertThat(documents.size).isEqualTo(1)
    }

  @Test
  @Transactional
  fun `given retention by capacity given, delete old items first`() {

    `when`(planConstraintsService.coerceRetentionMaxCapacity(any2(), any2())).thenReturn(1)
    documentUseCase.applyRetentionStrategyByCapacity()

    val documents = documentRepository.findAllByRepositoryId(repository.id)
    assertThat(documents.size).isEqualTo(3)
    assertThat(documents.filter { it.status == ReleaseStatus.unreleased }.size).isEqualTo(2)
    assertThat(documents.first { it.status == ReleaseStatus.released }.title).isEqualTo("future-released")
  }

}
