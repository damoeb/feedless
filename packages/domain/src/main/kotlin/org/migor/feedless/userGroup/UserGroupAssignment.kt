package org.migor.feedless.userGroup

import org.migor.feedless.group.GroupId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class UserGroupAssignment(
  val id: UserGroupAssignmentId = UserGroupAssignmentId(),
  val role: RoleInGroup,
  val userId: UserId,
  val groupId: GroupId,
  val createdAt: LocalDateTime = LocalDateTime.now(),
) {
}

