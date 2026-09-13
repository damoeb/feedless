package org.migor.feedless.group

import org.migor.feedless.userGroup.RoleInGroup

data class GroupAssignmentSummary(
  val groupId: GroupId,
  val role: RoleInGroup,
  val name: String,
)
