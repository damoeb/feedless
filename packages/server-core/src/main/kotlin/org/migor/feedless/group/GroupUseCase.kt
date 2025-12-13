package org.migor.feedless.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
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
  private val groupGuard: GroupGuard
) {

  private val log = LoggerFactory.getLogger(GroupUseCase::class.simpleName)


  suspend fun addUserToGroup(userId: UserId, group: Group, role: RoleInGroup): UserGroupAssignment =
    withContext(Dispatchers.IO) {
      log.info("[${coroutineContext.corrId()}] add user $userId to group: ${group.id}")
      groupGuard.requireWrite(group.id)

      val newAssigment = UserGroupAssignment(
        userId = userId,
        groupId = group.id,
        role = role
      )

      userGroupAssignmentRepository.save(newAssigment)
    }

  suspend fun removeUserFromGroup(user: User, group: Group) = withContext(Dispatchers.IO) {
    groupGuard.requireWrite(group.id)

    val assigment = userGroupAssignmentRepository.findByUserIdAndGroupId(user.id, group.id)
      ?: throw IllegalArgumentException("assignment not found")
    userGroupAssignmentRepository.delete(assigment)
  }

  suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment> = withContext(Dispatchers.IO) {
    userGroupAssignmentRepository.findAllByUserId(userId)
  }

}
