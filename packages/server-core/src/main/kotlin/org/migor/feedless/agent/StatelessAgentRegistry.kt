package org.migor.feedless.agent

import org.migor.feedless.AppProfiles
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.agent)
@ConditionalOnMissingBean(StatefulAgentRegistry::class)
class StatelessAgentRegistry: AgentRegistry {

  private val registry = mutableListOf<AgentEntity>()

  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UUID?): List<AgentEntity> {
    return registry.filter { it.ownerId == userId && it.openInstance }
  }

  override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID): AgentEntity? {
    return registry.find { it.connectionId == connectionId && it.secretKeyId == secretKeyId }
  }

  override suspend fun delete(agent: AgentEntity) {
    registry.remove(agent)
  }

  override suspend fun save(agent: AgentEntity) {
    registry.add(agent)
  }
}
