package org.migor.feedless.group

import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment

interface GroupUseCasePort {
  suspend fun create(name: String): Group

  suspend fun findByIdForUser(groupId: GroupId): Group?

  suspend fun delete(groupId: GroupId)

  suspend fun listAssignments(): List<GroupAssignmentSummary>

  suspend fun listMembers(groupId: GroupId, page: Int, pageSize: Int): List<UserGroupAssignment>

  suspend fun addUserToGroup(userId: UserId, groupId: GroupId, role: RoleInGroup): UserGroupAssignment

  suspend fun removeUserFromGroup(groupId: GroupId, userId: UserId)

  suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment>
}
