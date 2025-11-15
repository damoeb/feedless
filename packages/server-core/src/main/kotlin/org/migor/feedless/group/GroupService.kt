package org.migor.feedless.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.group.GroupEntity
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.data.jpa.userGroup.RoleInGroup
import org.migor.feedless.data.jpa.userGroup.UserGroupAssignmentDAO
import org.migor.feedless.data.jpa.userGroup.UserGroupAssignmentEntity
import org.migor.feedless.user.UserId
import org.migor.feedless.user.userId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.coroutines.coroutineContext

@Service
@Transactional(readOnly = true)
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GroupService(
  private val userGroupAssignmentDAO: UserGroupAssignmentDAO,
  private val userDAO: UserDAO,
) {

  private val log = LoggerFactory.getLogger(GroupService::class.simpleName)

//  fun getAdminGroupName(): String = "admin"

  @Transactional
  suspend fun addUserToGroup(user: UserEntity, group: GroupEntity, role: RoleInGroup): UserGroupAssignmentEntity {
    val currentUserId = coroutineContext.userId()

    return withContext(Dispatchers.IO) {
      assertCurrentUserHasPermissions(currentUserId, group)

      val newAssigment = UserGroupAssignmentEntity()
      newAssigment.userId = user.id
      newAssigment.groupId = group.id
      newAssigment.role = role

      userGroupAssignmentDAO.save(newAssigment)
    }
  }

  @Transactional
  suspend fun removeUserFromGroup(user: UserEntity, group: GroupEntity) {
    val currentUserId = coroutineContext.userId()

    withContext(Dispatchers.IO) {
      assertCurrentUserHasPermissions(currentUserId, group)

      val assigment = userGroupAssignmentDAO.findByUserIdAndGroupId(user.id, group.id)
        ?: throw IllegalArgumentException("assignment not found")
      userGroupAssignmentDAO.delete(assigment)
    }
  }

  private fun assertCurrentUserHasPermissions(currentUserId: UserId, group: GroupEntity) {
    val isAdmin = userDAO.findById(currentUserId.value).orElseThrow().admin

    if (!isAdmin) {
      val deniedException = PermissionDeniedException("")
      val currentUserPermissions =
        userGroupAssignmentDAO.findByUserIdAndGroupId(currentUserId.value, group.id) ?: throw deniedException
      if (RoleInGroup.owner != currentUserPermissions.role) {
        throw deniedException
      }
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignmentEntity> {
    return withContext(Dispatchers.IO) {
      userGroupAssignmentDAO.findAllByUserId(userId.value)
    }
  }
}
