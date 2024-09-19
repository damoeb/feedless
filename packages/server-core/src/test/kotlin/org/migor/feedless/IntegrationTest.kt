package org.migor.feedless

import PostgreSQLExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.locationtech.jts.geom.Point
import org.migor.feedless.attachment.AttachmentDAO
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.GroupService
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryHarvester
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.JtsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.data.domain.PageRequest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
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
@MockBeans(
  MockBean(SessionService::class),
  MockBean(ProductDAO::class),
  MockBean(RepositoryService::class),
  MockBean(HttpService::class),
  MockBean(PlanConstraintsService::class),
  MockBean(DocumentPipelineJobDAO::class),
  MockBean(PluginService::class),
  MockBean(FeatureService::class),
  MockBean(ProductService::class),
  MockBean(PropertyService::class),
  MockBean(RepositoryHarvester::class),
  MockBean(AttachmentDAO::class),
  MockBean(GroupService::class),
)
@Testcontainers
class IntegrationTest {

  lateinit var repository: RepositoryEntity

  @Autowired
  lateinit var documentService: DocumentService

  @Autowired
  lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var documentDAO: DocumentDAO

  val past = LocalDateTime.now().minusDays(1)
  val future = LocalDateTime.now().plusDays(1)

  @BeforeEach
  fun setUp() = runTest {
    val user = UserEntity()
    user.email = "test@test.com"
    userDAO.save(user)

    repository = createRepository("A", user)
    createRepository("B", user)
  }

  private fun addDocuments(it: RepositoryEntity) {
//    val plusTwoDays = LocalDateTime.now().plusDays(1)
    createDocument(
      it,
      status = ReleaseStatus.released,
      publishedAt = past,
      startingAt = past,
      createdAt = past,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
    createDocument(
      it,
      status = ReleaseStatus.unreleased,
      publishedAt = past,
      startingAt = past,
      createdAt = past,
      latlon = JtsUtil.createPoint(1.0, 1.0)
    )
    createDocument(
      it,
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
    repository.product = ProductCategory.rssProxy
    repository.ownerId = user.id

    return repositoryDAO.save(repository).also { addDocuments(it) }
  }

  private fun createDocument(
    repository: RepositoryEntity,
    status: ReleaseStatus,
    publishedAt: LocalDateTime,
    startingAt: LocalDateTime,
    createdAt: LocalDateTime,
    latlon: Point
  ) {
    val d = DocumentEntity()
    d.url = "http://localhost:8080"
    d.text = ""
    d.repositoryId = repository.id
    d.status = status
    d.publishedAt = publishedAt
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
    val page = documentService.findAllByRepositoryId(
      repository.id,
      null,
      null,
      ReleaseStatus.released,
      null,
      PageRequest.of(0, 10),
      null,
      false
    )
    val documents = page.toList()
    assertThat(documents.size).isEqualTo(1)
  }

}
