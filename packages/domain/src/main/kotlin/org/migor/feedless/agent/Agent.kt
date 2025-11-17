package org.migor.feedless.agent

import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime

data class Agent(
  val id: AgentId,
  val connectionId: String,
  val version: String,
  val openInstance: Boolean,
  val name: String,
  val lastSyncedAt: LocalDateTime,
  val secretKeyId: UserSecretId?,
  val ownerId: UserId,
  val createdAt: LocalDateTime
)

