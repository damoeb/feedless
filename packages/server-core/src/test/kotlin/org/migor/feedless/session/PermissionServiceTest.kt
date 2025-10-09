package org.migor.feedless.session

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.eq
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.*

class PermissionServiceTest {

  private lateinit var permissionService: PermissionService
  private lateinit var userDAO: UserDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private val currentUserId = randomUserId()
  private val repositoryId = randomRepositoryId()

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
    `when`(document.repositoryId).thenReturn(repositoryId.value)
    mockRepository(repositoryId, ownerId = currentUserId)
    permissionService.canWrite(document)
  }

  @Test
  fun `canWrite document for non-owner fails`() {
    assertThatExceptionOfType(PermissionDeniedException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId)) {
        val document = mock(DocumentEntity::class.java)
        `when`(document.repositoryId).thenReturn(repositoryId.value)
        mockRepository(repositoryId, ownerId = randomUserId())
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
        val repository = mockRepository(repositoryId, ownerId = randomUserId())
        permissionService.canWrite(repository)
      }
    }
  }

  private fun mockRepository(repositoryId: RepositoryId, ownerId: UserId): RepositoryEntity {
    val repository = mock(RepositoryEntity::class.java)
    `when`(repository.id).thenReturn(repositoryId.value)
    `when`(repository.ownerId).thenReturn(ownerId.value)
    `when`(repositoryDAO.findById(eq(repositoryId.value))).thenReturn(Optional.of(repository))
    return repository
  }

  private fun mockUser(userId: UserId): UserEntity {
    val user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId.value)
    `when`(userDAO.findById(eq(userId.value))).thenReturn(Optional.of(user))
    return user
  }
}
