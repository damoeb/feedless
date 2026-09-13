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
import org.migor.feedless.NotFoundException
import org.migor.feedless.any
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.ShareKeyAccess
import org.migor.feedless.eq
import org.migor.feedless.group.GroupId
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class RepositoryGuardTest {

  private lateinit var repositoryGuard: RepositoryGuard
  private lateinit var repositoryRepository: RepositoryRepository
  private val userId = randomUserId()
  private val member = UserId()
  private val stranger = UserId()
  private val groupId = GroupId()
  private val repositoryId = randomRepositoryId()
  private val bannedStranger = UserId()
  private val bannedMember = UserId()
  private lateinit var userRepository: UserRepository

  @BeforeEach
  fun setUp() = runTest {
    userRepository = mock(UserRepository::class.java)
    val user = mockUser(userId)
    `when`(userRepository.findById(any(UserId::class.java))).thenReturn(user)
    val banned = mockUser(bannedStranger)
    `when`(banned.banned).thenReturn(true)
    `when`(userRepository.findById(eq(bannedStranger))).thenReturn(banned)
    `when`(userRepository.findById(eq(bannedMember))).thenReturn(banned)

    val userGuard = UserGuard(userRepository)

    repositoryRepository = mock(RepositoryRepository::class.java)
    val repository = mockRepository(repositoryId, userId)
    `when`(repositoryRepository.findById(any(RepositoryId::class.java)))
      .thenReturn(repository)

    val userGroupAssignmentRepository = mock(UserGroupAssignmentRepository::class.java)
    `when`(userGroupAssignmentRepository.findAllByUserId(any(UserId::class.java))).thenReturn(emptyList())
    `when`(userGroupAssignmentRepository.findAllByUserId(eq(member))).thenReturn(
      listOf(UserGroupAssignment(role = RoleInGroup.viewer, userId = member, groupId = groupId)),
    )
    `when`(userGroupAssignmentRepository.findAllByUserId(eq(bannedMember))).thenReturn(
      listOf(UserGroupAssignment(role = RoleInGroup.viewer, userId = bannedMember, groupId = groupId)),
    )

    repositoryGuard = RepositoryGuard(repositoryRepository, userGuard, userGroupAssignmentRepository)
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
  fun `requireRead of a private repository works for its owner`() =
    runTest(context = asUser(userId)) {
      val repository = givenRepository()
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }

  @Test
  fun `requireRead of a private repository works for a member of its group`() =
    runTest(context = asUser(member)) {
      val repository = givenRepository()
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }

  @Test
  fun `requireRead of a private repository fails for a logged-in stranger like a missing repository`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeMissing(repository.id, asUser(stranger))
  }

  @Test
  fun `requireRead of a private repository fails for a banned stranger like a missing repository`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeMissing(repository.id, asUser(bannedStranger))
  }

  @Test
  fun `requireRead of a private repository denies a banned member`() {
    val repository = runBlocking { givenRepository() }
    assertThatThrownBy {
      runTest(context = asUser(bannedMember)) { repositoryGuard.requireRead(repository.id) }
    }.isInstanceOf(IllegalArgumentException::class.java).hasMessage("denied")
  }

  @Test
  fun `requireRead of a private repository fails without login like a missing repository`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeMissing(repository.id, EmptyCoroutineContext)
  }

  @Test
  fun `requireRead of a missing repository fails with not found`() {
    val missingId = RepositoryId()
    runBlocking { `when`(repositoryRepository.findById(eq(missingId))).thenReturn(null) }
    assertDeniedLikeMissing(missingId, asUser(userId))
  }

  @Test
  fun `requireRead of a private repository with its share key works without login`() = runTest {
    val repository = givenRepository()
    withContext(ShareKeyAccess(repository.id, SHARE_KEY)) {
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }
  }

  @Test
  fun `requireRead of a private repository with a wrong key fails like no key`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeMissing(repository.id, ShareKeyAccess(repository.id, "wrong-key"))
  }

  @Test
  fun `requireRead of a private repository with a blank key fails`() {
    val repository = runBlocking { givenRepository() }
    assertDeniedLikeMissing(repository.id, ShareKeyAccess(repository.id, " "))
  }

  @Test
  fun `requireRead of a private repository without a share key of its own fails`() {
    val repository = runBlocking { givenRepository(shareKey = "") }
    assertDeniedLikeMissing(repository.id, ShareKeyAccess(repository.id, ""))
  }

  @Test
  fun `requireRead with the key of a repository presented for another repository fails`() {
    val repository = runBlocking { givenRepository() }
    val other = runBlocking { givenRepository() }
    assertDeniedLikeMissing(repository.id, ShareKeyAccess(other.id, SHARE_KEY))
  }

  @Test
  fun `requireRead of a public repository works for anyone and ignores a wrong key`() = runTest {
    val repository = givenRepository(visibility = EntityVisibility.isPublic)
    assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    withContext(asUser(stranger)) {
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }
    withContext(ShareKeyAccess(repository.id, "wrong-key")) {
      assertThat(repositoryGuard.requireRead(repository.id)).isEqualTo(repository)
    }
  }

  @Test
  fun `requireReadGrant names the repository it was granted for`() = runTest(context = asUser(member)) {
    val repository = givenRepository()
    assertThat(repositoryGuard.requireReadGrant(repository.id).repositoryId).isEqualTo(repository.id)
  }

  @Test
  fun `requireReadGrant fails for a stranger`() {
    val repository = runBlocking { givenRepository() }
    assertThatThrownBy {
      runTest(context = asUser(stranger)) { repositoryGuard.requireReadGrant(repository.id) }
    }.isInstanceOf(NotFoundException::class.java)
  }

  @Test
  fun `configuration is readable by owner and members only, even on a public repository`() = runTest {
    val repository = givenRepository(visibility = EntityVisibility.isPublic)
    withContext(asUser(userId)) { assertThat(repositoryGuard.mayReadConfiguration(repository)).isTrue() }
    withContext(asUser(member)) { assertThat(repositoryGuard.mayReadConfiguration(repository)).isTrue() }
    withContext(asUser(stranger)) { assertThat(repositoryGuard.mayReadConfiguration(repository)).isFalse() }
    assertThat(repositoryGuard.mayReadConfiguration(repository)).isFalse()
    withContext(ShareKeyAccess(repository.id, SHARE_KEY)) {
      assertThat(repositoryGuard.mayReadConfiguration(repository)).isFalse()
    }
  }

  @Test
  fun `a share key does not grant write to a logged-in non-owner`() {
    val repository = runBlocking { givenRepository() }
    assertThatThrownBy {
      runTest(context = asUser(member) + ShareKeyAccess(repository.id, SHARE_KEY)) {
        repositoryGuard.requireWrite(repository.id)
      }
    }.isInstanceOf(IllegalArgumentException::class.java).hasMessage("must be owner")
  }

  @Test
  fun `a share key does not grant write without login`() {
    val repository = runBlocking { givenRepository() }
    // requireWrite needs a user id; without one it fails before looking at the repository
    assertThatThrownBy {
      runTest(context = ShareKeyAccess(repository.id, SHARE_KEY)) {
        repositoryGuard.requireWrite(repository.id)
      }
    }.isInstanceOf(NullPointerException::class.java)
  }

  private fun asUser(userId: UserId) = RequestContext(groupId = GroupId(), userId = userId)

  private fun assertDeniedLikeMissing(repositoryId: RepositoryId, context: CoroutineContext) {
    assertThatThrownBy {
      runTest(context = context) { repositoryGuard.requireRead(repositoryId) }
    }
      .isInstanceOf(NotFoundException::class.java)
      .hasMessage("Repository $repositoryId not found")
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
      groupId = groupId,
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
