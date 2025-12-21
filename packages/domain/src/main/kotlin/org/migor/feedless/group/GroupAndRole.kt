package org.migor.feedless.group

import kotlinx.serialization.Serializable
import org.migor.feedless.userGroup.RoleInGroup

@Serializable
data class GroupAndRole(
  val groupId: GroupId,
  val role: RoleInGroup,
)
