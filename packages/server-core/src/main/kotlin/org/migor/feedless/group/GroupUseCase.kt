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

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GroupUseCase(
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
  private val userRepository: UserRepository,
  private val capabilityService: CapabilityService,
) {

  private val log = LoggerFactory.getLogger(GroupUseCase::class.simpleName)

  private fun userId(): UserId {
    return capabilityService.getCapability(UserCapability.ID)?.let { UserCapability.resolve(it) }!!
  }


  suspend fun addUserToGroup(user: User, group: Group, role: RoleInGroup): UserGroupAssignment =
    withContext(Dispatchers.IO) {
      assertCurrentUserHasPermissions(userId(), group)

      val newAssigment = UserGroupAssignment(
        userId = user.id,
        groupId = group.id,
        role = role
      )

      userGroupAssignmentRepository.save(newAssigment)
    }

  suspend fun removeUserFromGroup(user: User, group: Group) = withContext(Dispatchers.IO) {
    assertCurrentUserHasPermissions(userId(), group)

    val assigment = userGroupAssignmentRepository.findByUserIdAndGroupId(user.id, group.id)
      ?: throw IllegalArgumentException("assignment not found")
    userGroupAssignmentRepository.delete(assigment)
  }

  private fun assertCurrentUserHasPermissions(currentUserId: UserId, group: Group) {
    val isAdmin = userRepository.findById(currentUserId)!!.admin

    if (!isAdmin) {
      val deniedException = PermissionDeniedException("")
      val currentUserPermissions =
        userGroupAssignmentRepository.findByUserIdAndGroupId(currentUserId, group.id)
          ?: throw deniedException
      if (RoleInGroup.owner != currentUserPermissions.role) {
        throw deniedException
      }
    }
  }

  suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment> = withContext(Dispatchers.IO) {
    userGroupAssignmentRepository.findAllByUserId(userId)
  }

}
