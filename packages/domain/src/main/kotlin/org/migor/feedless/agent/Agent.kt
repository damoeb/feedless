package org.migor.feedless.agent

import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime
import java.util.*

data class Agent(
    val id: AgentId = AgentId(UUID.randomUUID()),
    val connectionId: String,
    val version: String,
    val openInstance: Boolean,
    val name: String,
    val lastSyncedAt: LocalDateTime,
    val secretKeyId: UserSecretId? = null,
    val ownerId: UserId,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

