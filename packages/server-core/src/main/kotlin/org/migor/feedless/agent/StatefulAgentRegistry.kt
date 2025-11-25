package org.migor.feedless.agent

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.agent} & ${AppLayer.repository} & ${AppLayer.service}")
class StatefulAgentRegistry(
  private val agentDAO: AgentRepository
) : AgentRegistry {

  @Transactional(readOnly = true)
  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent> {
    return agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
  }

  @Transactional(readOnly = true)
  override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent? {
    return agentDAO.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId)
  }

  @Transactional
  override suspend fun delete(agent: Agent) {
    agentDAO.deleteById(agent.id)
  }

  @Transactional
  override suspend fun save(agent: Agent) {
    agentDAO.save(agent)
  }
}
