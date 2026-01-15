package org.migor.feedless.repository

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.any
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.eq
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class RepositoryGuardTest {

  private lateinit var repositoryGuard: RepositoryGuard
  private lateinit var userGuard: UserGuard
  private lateinit var repositoryRepository: RepositoryRepository
  private val userId = randomUserId()
  private val repositoryId = randomRepositoryId()

  @BeforeEach
  fun setUp() = runTest {
    val userRepository = mock(UserRepository::class.java)
    val user = mockUser(userId)
    `when`(userRepository.findById(any(UserId::class.java))).thenReturn(user)

    userGuard = UserGuard(userRepository)

    repositoryRepository = mock(RepositoryRepository::class.java)
    val repository = mockRepository(repositoryId, userId)
    `when`(repositoryRepository.findById(any(RepositoryId::class.java)))
      .thenReturn(repository)

    repositoryGuard = RepositoryGuard(repositoryRepository, userGuard)
  }

  @Test
  fun `canWrite repository for owner works`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
      repositoryGuard.requireWrite(repositoryId)
    }

  @Test
  fun `canWrite repository for non-owner fails`() {
    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(groupId = GroupId(), userId = UserId())) {
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
    return user
  }

}
