package org.migor.feedless.http

import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

/** The owner, an editor of the owning group and a stranger, stubbed into the ports the real guard reads. */
class RepositoryAccessFixture(
  private val repositoryUseCase: RepositoryUseCasePort,
  private val groupUseCase: GroupUseCase,
) {
  val owner = UserId()
  val member = UserId()
  val stranger = UserId()
  private val groupId = GroupId()

  suspend fun givenRepository(visibility: EntityVisibility = EntityVisibility.isPrivate): Repository {
    val repo = Repository(
      title = "Test feed",
      description = "desc",
      visibility = visibility,
      ownerId = owner,
      groupId = groupId,
      product = Vertical.rssProxy,
      shareKey = "share-key",
    )
    whenever(repositoryUseCase.findById(eq(repo.id))).thenReturn(repo)
    whenever(groupUseCase.findAllByUserId(eq(member))).thenReturn(
      listOf(UserGroupAssignment(role = RoleInGroup.editor, userId = member, groupId = groupId)),
    )
    whenever(groupUseCase.findAllByUserId(eq(stranger))).thenReturn(emptyList())
    return repo
  }
}
