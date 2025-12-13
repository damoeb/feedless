package org.migor.feedless.user

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.document.Document
import org.migor.feedless.eq
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.RequestContext
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class UserGuardTest {

  private lateinit var repositoryGuard: RepositoryGuard
  private lateinit var userGuard: UserGuard
  private lateinit var userRepository: UserRepository
  private lateinit var repositoryRepository: RepositoryRepository
  private val currentUserId = randomUserId()
  private val repositoryId = randomRepositoryId()

  @BeforeEach
  fun setUp() = runTest {
    userRepository = mock(UserRepository::class.java)
    repositoryRepository = mock(RepositoryRepository::class.java)
    userGuard = UserGuard(userRepository)
    repositoryGuard = RepositoryGuard(repositoryRepository, userGuard)

    mockUser(currentUserId)
  }

  @Test
  fun `canWrite document for owner works`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      val document = mock(Document::class.java)
      `when`(document.repositoryId).thenReturn(repositoryId)
      mockRepository(repositoryId, ownerId = currentUserId)
      repositoryGuard.requireWrite(repositoryId)
    }

  @Test
  fun `canWrite document for non-owner fails`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
        val document = mock(Document::class.java)
        `when`(document.repositoryId).thenReturn(repositoryId)
        mockRepository(repositoryId, ownerId = randomUserId())
        repositoryGuard.requireWrite(repositoryId)
      }
    }
  }

  @Test
  fun `canWrite repository for owner works`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = currentUserId)) {
      val repository = mockRepository(repositoryId, ownerId = currentUserId)
      repositoryGuard.requireWrite(repositoryId)
    }

  @Test
  fun `canWrite repository for non-owner fails`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = randomUserId())) {
        repositoryGuard.requireWrite(repositoryId)
      }
    }
  }

  private fun mockRepository(repositoryId: RepositoryId, ownerId: UserId): Repository {
    val repository = mock(Repository::class.java)
    `when`(repository.id).thenReturn(repositoryId)
    `when`(repository.ownerId).thenReturn(ownerId)
    `when`(repositoryRepository.findById(eq(repositoryId))).thenReturn(repository)
    return repository
  }

  private fun mockUser(userId: UserId): User {
    val user = mock(User::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(userRepository.findById(eq(userId))).thenReturn(user)
    return user
  }

}
