package org.migor.feedless.document

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.Vertical
import org.migor.feedless.any2
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.attachment.AttachmentDAO
import org.migor.feedless.data.jpa.document.DocumentDAO
import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.pipelineJob.DocumentPipelineJobDAO
import org.migor.feedless.data.jpa.product.ProductDAO
import org.migor.feedless.data.jpa.repository.RepositoryDAO
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.GroupService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductService
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.SessionService
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.JtsUtil
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
    SessionService::class,
    ProductDAO::class,
    RepositoryService::class,
    HttpService::class,
    DocumentPipelineJobDAO::class,
    PluginService::class,
    FeatureService::class,
    ProductService::class,
    PropertyService::class,
    RepositoryHarvester::class,
    AttachmentDAO::class,
    GroupService::class,
    PermissionService::class,
  ]
)
@Testcontainers
class DocumentIntTest {

  lateinit var repository: RepositoryEntity

  @Autowired
  lateinit var documentService: DocumentService

  @Autowired
  lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var documentDAO: DocumentDAO

  @MockitoBean
  lateinit var planConstraintsService: PlanConstraintsService

  val past = LocalDateTime.now().minusDays(1)
  val future = LocalDateTime.now().plusDays(1)

  @BeforeEach
  fun setUp() = runTest {
    // todo creat user
    val user = UserEntity()
    user.email = "test@test.com"
    userDAO.save(user)

    repository = createRepository("A", user)
    createRepository("B", user)

    assertThat(documentDAO.countByRepositoryId(repository.id)).isEqualTo(4)
  }

  private fun addDocuments(it: RepositoryEntity) {
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

  private fun createRepository(suffix: String, user: UserEntity): RepositoryEntity {
    val repository = RepositoryEntity()
    repository.title = "title $suffix"
    repository.description = "description $suffix"
    repository.sourcesSyncCron = ""
    repository.product = Vertical.rssProxy
    repository.ownerId = user.id
    repository.lastUpdatedAt = LocalDateTime.now().minusDays(2)

    return repositoryDAO.save(repository).also { addDocuments(it) }
  }

  private fun createDocument(
    repository: RepositoryEntity,
    title: String,
    status: ReleaseStatus,
    publishedAt: LocalDateTime,
    startingAt: LocalDateTime,
    createdAt: LocalDateTime,
    latlon: Point
  ) {
    val d = DocumentEntity()
    d.url = "http://localhost:8080"
    d.title = title
    d.text = ""
    d.repositoryId = repository.id
    d.status = status
    d.publishedAt = publishedAt
    d.contentHash = CryptUtil.sha1(newCorrId())
    d.startingAt = startingAt
    d.createdAt = createdAt
    d.latLon = latlon

    documentDAO.save(d)
  }

  @BeforeEach
  fun tearDown() {
    userDAO.deleteAll()
  }

  @Test
  fun `given where is null, findAll filters repoId and status`() = runTest {
    val documents = documentService.findAllByRepositoryId(
      repositoryId = RepositoryId(repository.id),
      status = ReleaseStatus.released,
      pageable = PageRequest.of(0, 10),
    )
    assertThat(documents.size).isEqualTo(1)
  }

  @Test
  fun `given retention by capacity given, delete old items first`() = runTest {

    `when`(planConstraintsService.coerceRetentionMaxCapacity(any2(), any2(), any2())).thenReturn(1)
    documentService.applyRetentionStrategyByCapacity()

    val documents = documentDAO.findAllByRepositoryId(repository.id)
    assertThat(documents.size).isEqualTo(3)
    assertThat(documents.filter { it.status == ReleaseStatus.unreleased }.size).isEqualTo(2)
    assertThat(documents.first { it.status == ReleaseStatus.released }.title).isEqualTo("future-released")
  }

}
