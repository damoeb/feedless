package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCase
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RepositoryAccessGuardTest {

  private val repositoryUseCase: RepositoryUseCase = mock()
  private val sourceRepository: SourceRepository = mock()
  private val groupUseCase: GroupUseCase = mock()
  private val guard = RepositoryAccessGuard(repositoryUseCase, sourceRepository, groupUseCase)

  private val owner = UserId()
  private val stranger = UserId()
  private val groupId = GroupId()

  @Test
  fun `the owner may read and write a private repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)

    assert(asUser(owner) { guard.requireRepository(repo.id, RepositoryAccess.read) } == repo)
    assert(asUser(owner) { guard.requireRepository(repo.id, RepositoryAccess.write) } == repo)
    // The owner check alone decides; no membership lookup needed.
    verify(groupUseCase, never()).findAllByUserId(any())
  }

  @Test
  fun `a group editor may read and write a private repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    val member = givenMember(RoleInGroup.editor)

    assert(asUser(member) { guard.requireRepository(repo.id, RepositoryAccess.read) } == repo)
    assert(asUser(member) { guard.requireRepository(repo.id, RepositoryAccess.write) } == repo)
  }

  @Test
  fun `a group owner may write a private repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    val member = givenMember(RoleInGroup.owner)

    assert(asUser(member) { guard.requireRepository(repo.id, RepositoryAccess.write) } == repo)
  }

  @Test
  fun `a group viewer may read but not write a private repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    val member = givenMember(RoleInGroup.viewer)

    assert(asUser(member) { guard.requireRepository(repo.id, RepositoryAccess.read) } == repo)
    assertNotFound { asUser(member) { guard.requireRepository(repo.id, RepositoryAccess.write) } }
  }

  @Test
  fun `a member of another group is a stranger`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(
      listOf(UserGroupAssignment(role = RoleInGroup.owner, userId = stranger, groupId = GroupId())),
    )

    assertNotFound { asUser(stranger) { guard.requireRepository(repo.id, RepositoryAccess.read) } }
  }

  @Test
  fun `a stranger may neither read nor write a private repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(emptyList())

    assertNotFound { asUser(stranger) { guard.requireRepository(repo.id, RepositoryAccess.read) } }
    assertNotFound { asUser(stranger) { guard.requireRepository(repo.id, RepositoryAccess.write) } }
  }

  @Test
  fun `a stranger may read but not write a public repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPublic)
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(emptyList())

    assert(asUser(stranger) { guard.requireRepository(repo.id, RepositoryAccess.read) } == repo)
    assertNotFound { asUser(stranger) { guard.requireRepository(repo.id, RepositoryAccess.write) } }
  }

  @Test
  fun `a denied repository is indistinguishable from a missing one`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(emptyList())
    val missingId = RepositoryId()
    whenever(repositoryUseCase.findById(eq(missingId))).thenReturn(null)

    val denied = assertNotFound { asUser(stranger) { guard.requireRepository(repo.id, RepositoryAccess.read) } }
    val missing = assertNotFound { asUser(stranger) { guard.requireRepository(missingId, RepositoryAccess.read) } }

    assert(denied.message == "repository ${repo.id.uuid} not found") { denied.message!! }
    assert(missing.message == "repository ${missingId.uuid} not found") { missing.message!! }
  }

  @Test
  fun `a caller without a user id is denied even a public repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPublic)

    assertNotFound { withContext(RequestContext(userId = null)) { guard.requireRepository(repo.id, RepositoryAccess.read) } }
    assertNotFound { guard.requireRepository(repo.id, RepositoryAccess.read) }
    verify(repositoryUseCase, never()).findById(any())
  }

  @Test
  fun `the configuration of a public repository is for the owner and group members only`() = runTest {
    val repo = givenRepository(EntityVisibility.isPublic)
    val viewer = givenMember(RoleInGroup.viewer)
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(emptyList())

    assert(asUser(owner) { guard.requireRepositoryConfiguration(repo.id) } == repo)
    assert(asUser(viewer) { guard.requireRepositoryConfiguration(repo.id) } == repo)
    assertNotFound { asUser(stranger) { guard.requireRepositoryConfiguration(repo.id) } }
    assertNotFound { guard.requireRepositoryConfiguration(repo.id) }
  }

  @Test
  fun `requireSource returns a source of an accessible repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    val source = givenSource(repo.id)

    assert(asUser(owner) { guard.requireSource(repo.id, source.id, RepositoryAccess.write) } == source)
  }

  @Test
  fun `requireSource checks the repository before it looks up the source`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    val source = givenSource(repo.id)
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(emptyList())

    val ex = assertNotFound { asUser(stranger) { guard.requireSource(repo.id, source.id, RepositoryAccess.read) } }

    assert(ex.message == "repository ${repo.id.uuid} not found") { ex.message!! }
    verify(sourceRepository, never()).findByIdWithActions(any())
  }

  @Test
  fun `requireSource answers not found for a source of another repository`() = runTest {
    val repo = givenRepository(EntityVisibility.isPrivate)
    val foreign = givenSource(RepositoryId())
    val missingId = SourceId()
    whenever(sourceRepository.findByIdWithActions(eq(missingId))).thenReturn(null)

    val foreignEx = assertNotFound { asUser(owner) { guard.requireSource(repo.id, foreign.id, RepositoryAccess.read) } }
    val missingEx = assertNotFound { asUser(owner) { guard.requireSource(repo.id, missingId, RepositoryAccess.read) } }

    // Same wording for both, so a foreign source's existence does not leak either.
    assert(foreignEx.message == "source ${foreign.id.uuid} not found") { foreignEx.message!! }
    assert(missingEx.message == "source ${missingId.uuid} not found") { missingEx.message!! }
  }

  @Test
  fun `requireCallerScope resolves the caller's own id and its group memberships`() = runTest {
    val member = givenMember(RoleInGroup.viewer)

    val (userId, groupIds) = asUser(member) { guard.requireCallerScope() }

    assert(userId == member) { userId }
    assert(groupIds == listOf(groupId)) { groupIds }
  }

  @Test
  fun `requireCallerScope returns no groups for a caller in none`() = runTest {
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(emptyList())

    val (userId, groupIds) = asUser(stranger) { guard.requireCallerScope() }

    assert(userId == stranger) { userId }
    assert(groupIds.isEmpty()) { groupIds }
  }

  @Test
  fun `requireCallerScope answers a caller without a user id like a denied repository`() = runTest {
    assertNotFound { withContext(RequestContext(userId = null)) { guard.requireCallerScope() } }
    assertNotFound { guard.requireCallerScope() }
    verify(groupUseCase, never()).findAllByUserId(any())
  }

  private suspend fun givenRepository(visibility: EntityVisibility): Repository {
    val repo = Repository(
      title = "feed",
      visibility = visibility,
      ownerId = owner,
      groupId = groupId,
    )
    whenever(repositoryUseCase.findById(eq(repo.id))).thenReturn(repo)
    return repo
  }

  private suspend fun givenMember(role: RoleInGroup): UserId {
    val member = UserId()
    whenever(groupUseCase.findAllByUserId(eq(member))).thenReturn(
      listOf(UserGroupAssignment(role = role, userId = member, groupId = groupId)),
    )
    return member
  }

  private fun givenSource(repositoryId: RepositoryId): Source {
    val id = SourceId()
    val source = Source(
      id = id,
      title = "source",
      repositoryId = repositoryId,
      actions = listOf(FetchAction(sourceId = id, url = "https://example.com")),
    )
    whenever(sourceRepository.findByIdWithActions(eq(id))).thenReturn(source)
    return source
  }

  private suspend fun <T> asUser(userId: UserId, block: suspend () -> T): T =
    withContext(RequestContext(userId = userId, groupId = GroupId())) { block() }

  private suspend fun assertNotFound(block: suspend () -> Unit): NotFoundException {
    var thrown: Throwable? = null
    try {
      block()
    } catch (e: Throwable) {
      thrown = e
    }
    assert(thrown is NotFoundException) { "expected NotFoundException, got $thrown" }
    return thrown as NotFoundException
  }
}
