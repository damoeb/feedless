package org.migor.feedless.agent

import org.migor.feedless.data.jpa.agent.AgentEntity
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.stereotype.Service
import java.util.*

@Service
interface AgentRegistry {
  suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent>
  suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent?
  suspend fun delete(agent: Agent)
  suspend fun save(agent: Agent)
}
