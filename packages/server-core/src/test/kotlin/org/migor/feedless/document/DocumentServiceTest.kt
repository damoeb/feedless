package org.migor.feedless.document

import jakarta.persistence.EntityManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.generated.types.StringFilter
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.any
import org.migor.feedless.user.UserEntity
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.transaction.PlatformTransactionManager
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentServiceTest {

  private lateinit var repositoryDAO: RepositoryDAO

  private lateinit var documentService: DocumentService

  private var corrId = "test"

  private lateinit var currentUser: UserEntity

  @BeforeEach
  fun setUp() {
    currentUser = mock(UserEntity::class.java)
    `when`(currentUser.id).thenReturn(UUID.randomUUID())

    repositoryDAO = mock(RepositoryDAO::class.java)

    documentService = DocumentService(
      mock(DocumentDAO::class.java),
      mock(PlatformTransactionManager::class.java),
      mock(EntityManager::class.java),
      repositoryDAO,
      mock(PlanConstraintsService::class.java),
      mock(DocumentPipelineJobDAO::class.java),
      mock(PluginService::class.java)
    )
  }

//  @Test
//  fun `applyRetentionStrategy by capacity is skipped if plan returns null or 0`() {
//    TODO()
//  }
//
//  @Test
//  fun `applyRetentionStrategy by age is skipped if plan returns null`() {
//    TODO()
//  }

  @Test
  fun `given deleteDocuments is executed not by the owner, it fails`() = runTest {
    val repository = mock(RepositoryEntity::class.java)
    val repositoryId = UUID.randomUUID()
    `when`(repository.id).thenReturn(repositoryId)

    `when`(repository.ownerId).thenReturn(UUID.randomUUID())
    `when`(repositoryDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(repository))

    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runBlocking {
        documentService.deleteDocuments(corrId, currentUser, repositoryId, StringFilter())
      }
    }
  }
}
