package org.migor.feedless.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.agent} & ${AppLayer.repository} & ${AppLayer.service}")
class StatefulAgentRegistry(
  private val agentRepository: AgentRepository
) : AgentRegistry {

  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent> {
    return withContext(Dispatchers.IO) {
      agentRepository.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
    }
  }

  override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent? =
    withContext(Dispatchers.IO) {
      agentRepository.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId)
    }

  override suspend fun delete(agent: Agent) = withContext(Dispatchers.IO) {
    agentRepository.deleteById(agent.id)
  }

  override suspend fun save(agent: Agent): Agent = withContext(Dispatchers.IO) {
    agentRepository.save(agent)
  }
}
