package org.migor.feedless.group

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Group(
  val id: GroupId,
  val name: String,
  val ownerId: UserId,
  val createdAt: LocalDateTime
)

