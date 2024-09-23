package org.migor.feedless.session

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.eq
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.*

class PermissionServiceTest {

  private lateinit var permissionService: PermissionService
  private lateinit var userDAO: UserDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private val currentUserId = UUID.randomUUID()
  private val repositoryId = UUID.randomUUID()

  @BeforeEach
  fun setUp() {
    userDAO = mock(UserDAO::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)
    permissionService = PermissionService(userDAO, repositoryDAO)

    mockUser(currentUserId)
  }

  @Test
  fun `canWrite document for owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
    val document = mock(DocumentEntity::class.java)
    `when`(document.repositoryId).thenReturn(repositoryId)
    mockRepository(repositoryId, ownerId = currentUserId)
    permissionService.canWrite(document)
  }

  @Test
  fun `canWrite document for non-owner fails`() {
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        val document = mock(DocumentEntity::class.java)
        `when`(document.repositoryId).thenReturn(repositoryId)
        mockRepository(repositoryId, ownerId = UUID.randomUUID())
        permissionService.canWrite(document)
      }
    }
  }

  @Test
  fun `canWrite repository for owner works`() = runTest(context = RequestContext(userId = currentUserId)) {
    val repository = mockRepository(repositoryId, ownerId = currentUserId)
    permissionService.canWrite(repository)
  }

  @Test
  fun `canWrite repository for non-owner fails`() {
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        val repository = mockRepository(repositoryId, ownerId = UUID.randomUUID())
        permissionService.canWrite(repository)
      }
    }
  }

  private fun mockRepository(repositoryId: UUID, ownerId: UUID): RepositoryEntity {
    val repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(repositoryId)
    `when`(repository.ownerId).thenReturn(ownerId)
    `when`(repositoryDAO.findById(eq(repositoryId))).thenReturn(Optional.of(repository))
    return repository
  }

  private fun mockUser(userId: UUID): UserEntity {
    val user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(userDAO.findById(eq(userId))).thenReturn(Optional.of(user))
    return user
  }
}
