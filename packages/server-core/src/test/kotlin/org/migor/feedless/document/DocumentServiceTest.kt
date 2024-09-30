package org.migor.feedless.document

import jakarta.persistence.EntityManager
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.generated.types.CreateRecordInput
import org.migor.feedless.generated.types.RecordUniqueWhereInput
import org.migor.feedless.generated.types.RecordUpdateInput
import org.migor.feedless.generated.types.RepositoryUniqueWhereInput
import org.migor.feedless.generated.types.StringFilter
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.MaxAgeDaysDateField
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.any
import org.migor.feedless.repository.eq
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentServiceTest {

  private lateinit var documentDAO: DocumentDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var userDAO: UserDAO

  private lateinit var documentService: DocumentService

  private lateinit var currentUser: UserEntity
  private lateinit var permissionService: PermissionService
  private lateinit var planConstraintsService: PlanConstraintsService

  private val currentUserId = UUID.randomUUID()

  @BeforeEach
  fun setUp() {
    currentUser = mock(UserEntity::class.java)
    `when`(currentUser.id).thenReturn(UUID.randomUUID())

    userDAO = mock(UserDAO::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)
    documentDAO = mock(DocumentDAO::class.java)
    permissionService = PermissionService(userDAO, repositoryDAO)
    planConstraintsService = mock(PlanConstraintsService::class.java)
    documentService = DocumentService(
      documentDAO,
      mock(PlatformTransactionManager::class.java),
      mock(EntityManager::class.java),
      repositoryDAO,
      planConstraintsService,
      mock(DocumentPipelineJobDAO::class.java),
      mock(PluginService::class.java),
      permissionService,
    )
  }

//  @Test
//  fun `applyRetentionStrategy by capacity is skipped if plan returns null or 0`() {
//    TODO()
//  }

//  @Test
//  fun `applyRetentionStrategy by age is skipped if plan returns null`() {
//    TODO()
//  }

  @Test
  fun `applyRetentionStrategy by startingAt`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = UUID.randomUUID()
    mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.startingAt)

    // when
    documentService.applyRetentionStrategy(repositoryId)

    // then
    verify(documentDAO).deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(eq(repositoryId), any(LocalDateTime::class.java), any(
      ReleaseStatus::class.java))
  }
  @Test
  fun `applyRetentionStrategy by createdAt`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = UUID.randomUUID()
    mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.createdAt)

    // when
    documentService.applyRetentionStrategy(repositoryId)

    // then
    verify(documentDAO).deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(eq(repositoryId), any(LocalDateTime::class.java), any(
      ReleaseStatus::class.java))
  }
  @Test
  fun `applyRetentionStrategy by publishedAt`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = UUID.randomUUID()
    mockRepository(repositoryId, currentUserId, maxAgeDaysDateField = MaxAgeDaysDateField.publishedAt)

    // when
    documentService.applyRetentionStrategy(repositoryId)

    // then
    verify(documentDAO).deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(eq(repositoryId), any(LocalDateTime::class.java), any(
      ReleaseStatus::class.java))
  }

  @Test
  fun `create document without permissions fails`() {
    val documentId = UUID.randomUUID()
    val repositoryId = UUID.randomUUID()

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        mockUser(currentUserId)
        mockDocument(documentId = documentId, repositoryId = repositoryId)
        mockRepository(repositoryId, ownerId = UUID.randomUUID())

        val data = CreateRecordInput(
          title = "foo",
          publishedAt = Date().time,
          url = "",
          text = "",
          repositoryId = RepositoryUniqueWhereInput(id = repositoryId.toString()),
        )
        documentService.createDocument(data)
      }
    }
  }

  @Test
  fun `create document of owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repositoryId = UUID.randomUUID()

    mockUser(currentUserId)
    `when`(documentDAO.save(any(DocumentEntity::class.java))).thenAnswer { it.arguments[0] }
    mockRepository(repositoryId, ownerId = currentUserId)

    val data = CreateRecordInput(
      title = "foo",
      publishedAt = Date().time,
      url = "",
      text = "",
      repositoryId = RepositoryUniqueWhereInput(id = repositoryId.toString()),
    )
    documentService.createDocument(data)

    verify(documentDAO).save(any(DocumentEntity::class.java))
  }

  @Test
  fun `update document without permissions fails`() {
    val documentId = UUID.randomUUID()
    val repositoryId = UUID.randomUUID()

    mockUser(currentUserId)
    mockDocument(documentId = documentId, repositoryId = repositoryId)

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        mockRepository(repositoryId, ownerId = UUID.randomUUID())

        val data = RecordUpdateInput()
        val where = RecordUniqueWhereInput(id = documentId.toString())
        documentService.updateDocument(data, where)
      }
    }
  }

  @Test
  fun `update document of owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
    val documentId = UUID.randomUUID()
    val repositoryId = UUID.randomUUID()

    mockUser(currentUserId)
    val document = mockDocument(documentId = documentId, repositoryId = repositoryId)
    mockRepository(repositoryId, ownerId = currentUserId)
    `when`(documentDAO.save(any(DocumentEntity::class.java))).thenAnswer { it.arguments[0] }

    val data = RecordUpdateInput()
    val where = RecordUniqueWhereInput(id = documentId.toString())
    documentService.updateDocument(data, where)

    verify(documentDAO).save(eq(document))
  }


  @Test
  fun `given deleteDocuments is executed not by the owner, it fails`() {
    val repository = mock(RepositoryEntity::class.java)
    val repositoryId = UUID.randomUUID()
    `when`(repository.id).thenReturn(repositoryId)

    `when`(repository.ownerId).thenReturn(UUID.randomUUID())
    `when`(repositoryDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(repository))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest {
        documentService.deleteDocuments(currentUser, repositoryId, StringFilter())
      }
    }
  }

  private fun mockUser(userId: UUID): UserEntity {
    val user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(userDAO.findById(eq(userId))).thenReturn(Optional.of(user))
    return user
  }

  private fun mockDocument(documentId: UUID, repositoryId: UUID): DocumentEntity {
    val document = mock(DocumentEntity::class.java)
    `when`(document.id).thenReturn(documentId)
    `when`(document.repositoryId).thenReturn(repositoryId)
    `when`(documentDAO.findById(eq(documentId))).thenReturn(Optional.of(document))
    return document
  }

  private suspend fun mockRepository(repositoryId: UUID, ownerId: UUID, maxAgeDaysDateField: MaxAgeDaysDateField? = null): RepositoryEntity {
    val repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(repositoryId)
    `when`(repository.ownerId).thenReturn(ownerId)
    maxAgeDaysDateField?.let {
      `when`(repository.retentionMaxAgeDaysReferenceField).thenReturn(it)
    }
    `when`(repository.retentionMaxAgeDays).thenReturn(20)
    `when`(repository.id).thenReturn(repositoryId)
    `when`(repository.product).thenReturn(ProductCategory.feedless)
    `when`(repositoryDAO.findById(eq(repositoryId))).thenReturn(Optional.of(repository))

    `when`(planConstraintsService.coerceRetentionMaxAgeDays(
      repository.retentionMaxAgeDays,
      repository.ownerId,
      repository.product
    )).thenReturn(20)

    return repository
  }
}
