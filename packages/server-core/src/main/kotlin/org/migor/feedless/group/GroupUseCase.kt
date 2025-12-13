package org.migor.feedless.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.corrId
import org.migor.feedless.user.userId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GroupUseCase(
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
  private val userRepository: UserRepository
) {

  private val log = LoggerFactory.getLogger(GroupUseCase::class.simpleName)


  suspend fun addUserToGroup(userId: UserId, group: Group, role: RoleInGroup): UserGroupAssignment =
    withContext(Dispatchers.IO) {
      log.info("[${coroutineContext.corrId()}] add user $userId to group: ${group.id}")
      assertCurrentUserHasPermissions(coroutineContext.userId(), group)

      val newAssigment = UserGroupAssignment(
        userId = userId,
        groupId = group.id,
        role = role
      )

      userGroupAssignmentRepository.save(newAssigment)
    }

  suspend fun removeUserFromGroup(user: User, group: Group) = withContext(Dispatchers.IO) {
    assertCurrentUserHasPermissions(coroutineContext.userId(), group)

    val assigment = userGroupAssignmentRepository.findByUserIdAndGroupId(user.id, group.id)
      ?: throw IllegalArgumentException("assignment not found")
    userGroupAssignmentRepository.delete(assigment)
  }

  private fun assertCurrentUserHasPermissions(currentUserId: UserId, group: Group) {
    val isAdmin = userRepository.findById(currentUserId)!!.admin

    if (!isAdmin && group.ownerId != currentUserId) {
      val currentUserPermissions =
        userGroupAssignmentRepository.findByUserIdAndGroupId(currentUserId, group.id)
          ?: throw PermissionDeniedException("user does not belong to this group")
      if (RoleInGroup.owner != currentUserPermissions.role) {
        throw PermissionDeniedException("user is not owner of this group")
      }
    }
    log.info("user has permissions")
  }

  suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment> = withContext(Dispatchers.IO) {
    userGroupAssignmentRepository.findAllByUserId(userId)
  }

}
