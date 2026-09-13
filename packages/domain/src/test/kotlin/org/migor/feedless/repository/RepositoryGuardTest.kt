package org.migor.feedless.repository

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.any
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.ShareKeyAccess
import org.migor.feedless.eq
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.security.access.AccessDeniedException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

  @Test
  fun `requireRead of a private repository with its share key works without login`() = runTest {
    val repository = givenRepository()
    withContext(ShareKeyAccess(repository.id, SHARE_KEY)) {
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }
  }

  @Test
  fun `requireRead of a private repository without a key fails`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeNoKey(repository.id, EmptyCoroutineContext)
  }

  @Test
  fun `requireRead of a private repository with a wrong key fails like no key`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeNoKey(repository.id, ShareKeyAccess(repository.id, "wrong-key"))
  }

  @Test
  fun `requireRead of a private repository with a blank key fails`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeNoKey(repository.id, ShareKeyAccess(repository.id, " "))
  }

  @Test
  fun `requireRead of a private repository without a share key of its own fails`() {
    val repository = runBlocking { givenRepository(shareKey = "") }
    assertDeniedLikeNoKey(repository.id, ShareKeyAccess(repository.id, ""))
  }

  @Test
  fun `requireRead with the key of a repository presented for another repository fails`() {
    val repository = runBlocking { givenRepository() }
    val other = runBlocking { givenRepository() }
    assertDeniedLikeNoKey(repository.id, ShareKeyAccess(other.id, SHARE_KEY))
  }

  @Test
  fun `requireRead of a public repository ignores a wrong key`() = runTest {
    val repository = givenRepository(visibility = EntityVisibility.isPublic)
    withContext(ShareKeyAccess(repository.id, "wrong-key")) {
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }
  }

  @Test
  fun `requireRead of a private repository for the logged-in owner works without a key`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = userId)) {
      val repository = givenRepository()
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }

  @Test
  fun `a share key does not grant write`() {
    val repository = runBlocking { givenRepository() }
    assertThatThrownBy {
      runTest(context = ShareKeyAccess(repository.id, SHARE_KEY)) {
        repositoryGuard.requireWrite(repository.id)
      }
    }.isNotNull()
  }

  private fun assertDeniedLikeNoKey(repositoryId: RepositoryId, context: CoroutineContext) {
    assertThatThrownBy {
      runTest(context = context) { repositoryGuard.requireRead(repositoryId) }
    }
      .isInstanceOf(AccessDeniedException::class.java)
      .hasMessage("Repository $repositoryId is private, you are not logged in")
  }

  private suspend fun givenRepository(
    shareKey: String = SHARE_KEY,
    visibility: EntityVisibility = EntityVisibility.isPrivate,
  ): Repository {
    val repository = Repository(
      title = "feed",
      visibility = visibility,
      shareKey = shareKey,
      ownerId = userId,
      groupId = GroupId(),
    )
    `when`(repositoryRepository.findById(eq(repository.id))).thenReturn(repository)
    return repository
  }

  private companion object {
    const val SHARE_KEY = "s3cr3t-key"
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
