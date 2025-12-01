package org.migor.feedless.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GroupService(
  private val userGroupAssignmentDAO: UserGroupAssignmentRepository,
  private val userDAO: UserRepository,
  private val groupDAO: GroupRepository,
  private val capabilityService: CapabilityService,
) {

  private val log = LoggerFactory.getLogger(GroupService::class.simpleName)

//  fun getAdminGroupName(): String = "admin"

  private fun userId(): UserId {
    return capabilityService.getCapability(UserCapability.ID)?.let { UserCapability.resolve(it) }!!
  }


  @Transactional
  suspend fun addUserToGroup(user: User, group: Group, role: RoleInGroup): UserGroupAssignment {

    return withContext(Dispatchers.IO) {
      assertCurrentUserHasPermissions(userId(), group)

      val newAssigment = UserGroupAssignment(
        userId = user.id,
        groupId = group.id,
        role = role
      )

      userGroupAssignmentDAO.save(newAssigment)
    }
  }

  @Transactional
  suspend fun removeUserFromGroup(user: User, group: Group) {
    assertCurrentUserHasPermissions(userId(), group)

    val assigment = userGroupAssignmentDAO.findByUserIdAndGroupId(user.id, group.id)
      ?: throw IllegalArgumentException("assignment not found")
    userGroupAssignmentDAO.delete(assigment)
  }

  private suspend fun assertCurrentUserHasPermissions(currentUserId: UserId, group: Group) {
    val isAdmin = userDAO.findById(currentUserId)!!.admin

    if (!isAdmin) {
      val deniedException = PermissionDeniedException("")
      val currentUserPermissions =
        userGroupAssignmentDAO.findByUserIdAndGroupId(currentUserId, group.id)
          ?: throw deniedException
      if (RoleInGroup.owner != currentUserPermissions.role) {
        throw deniedException
      }
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment> {
    return userGroupAssignmentDAO.findAllByUserId(userId)
  }

  @Transactional(readOnly = true)
  suspend fun findById(groupId: GroupId): Group? {
    return groupDAO.findById(groupId)
  }
}
