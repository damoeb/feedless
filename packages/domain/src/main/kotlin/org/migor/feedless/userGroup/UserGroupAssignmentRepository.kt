package org.migor.feedless.userGroup

import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId

interface UserGroupAssignmentRepository {

  suspend fun findAllByUserId(userId: UserId): List<UserGroupAssignment>
  suspend fun findByUserIdAndGroupId(userId: UserId, groupId: GroupId): UserGroupAssignment?
  suspend fun save(assignment: UserGroupAssignment): UserGroupAssignment
  suspend fun delete(assignment: UserGroupAssignment)
}
