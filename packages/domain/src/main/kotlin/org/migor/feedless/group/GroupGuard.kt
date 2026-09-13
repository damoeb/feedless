package org.migor.feedless.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.guard.ResourceGuard
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.userId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GroupGuard(
  private val groupRepository: GroupRepository,
  private val userGuard: UserGuard,
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository
) : ResourceGuard<GroupId, Group> {

  private val log = LoggerFactory.getLogger(GroupGuard::class.simpleName)

  override suspend fun requireRead(id: GroupId): Group {
    TODO("Not yet implemented")
  }

  override suspend fun requireWrite(id: GroupId): Group = withContext(Dispatchers.IO) {
    val userId = coroutineContext.userId()
    val user = userGuard.requireRead(userId)
    val isAdmin = user.admin

    val group = groupRepository.findById(id) ?: throw PermissionDeniedException("group not found")

    if (!isAdmin && group.ownerId != userId) {
      val currentUserPermissions =
        userGroupAssignmentRepository.findByUserIdAndGroupId(userId, group.id)
          ?: throw PermissionDeniedException("user does not belong to this group")
      if (RoleInGroup.owner != currentUserPermissions.role) {
        throw PermissionDeniedException("user is not owner of this group")
      }
    }
    log.info("user has permissions")
    group
  }

  override suspend fun requireExecute(id: GroupId): Group {
    TODO("Not yet implemented")
  }

}
