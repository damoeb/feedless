package org.migor.feedless.agent

import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime

interface AgentRepository {
  suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent>
  suspend fun deleteAllByLastSyncedAtBefore(date: LocalDateTime)
  suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent?
  suspend fun saveAll(agents: List<Agent>): List<Agent>
  suspend fun deleteById(id: AgentId)
  suspend fun save(agent: Agent): Agent
}
