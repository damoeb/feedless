package org.migor.feedless.agent

import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime

interface AgentRepository {
  fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent>
  fun deleteAllByLastSyncedAtBefore(date: LocalDateTime)
  fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent?
  fun saveAll(agents: List<Agent>): List<Agent>
  fun deleteById(id: AgentId)
  fun save(agent: Agent): Agent
}
