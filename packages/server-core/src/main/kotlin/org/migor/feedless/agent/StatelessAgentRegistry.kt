package org.migor.feedless.agent

import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.agent)
@ConditionalOnMissingBean(StatefulAgentRegistry::class)
class StatelessAgentRegistry : AgentRegistry {

  private val registry = mutableListOf<Agent>()

  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent> {
    return registry.filter { it.ownerId == userId && it.openInstance }
  }

  override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent? {
    return registry.find { it.connectionId == connectionId && it.secretKeyId == secretKeyId }
  }

  override suspend fun delete(agent: Agent) {
    registry.remove(agent)
  }

  override suspend fun save(agent: Agent): Agent {
    registry.add(agent)
    return agent
  }
}
