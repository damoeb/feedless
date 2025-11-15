package org.migor.feedless.agent

import org.migor.feedless.jpa.agent.AgentEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
interface AgentRegistry {
  suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UUID?): List<AgentEntity>
  suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID): AgentEntity?
  suspend fun delete(agent: AgentEntity)
  suspend fun save(agent: AgentEntity)
}
