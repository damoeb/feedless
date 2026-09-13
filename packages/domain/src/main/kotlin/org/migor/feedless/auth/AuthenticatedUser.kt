package org.migor.feedless.auth

import org.migor.feedless.group.GroupAssignmentSummary
import org.migor.feedless.user.UserId

data class AuthenticatedUser(
  val id: UserId,
  val email: String,
  val groups: List<GroupAssignmentSummary>,
)
