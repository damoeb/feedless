package org.migor.feedless.userGroup

import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId

interface UserGroupAssignmentRepository {

  fun findAllByUserId(userId: UserId): List<UserGroupAssignment>
  fun findByUserIdAndGroupId(userId: UserId, groupId: GroupId): UserGroupAssignment?
  fun save(assignment: UserGroupAssignment): UserGroupAssignment
  fun delete(assignment: UserGroupAssignment)
}
