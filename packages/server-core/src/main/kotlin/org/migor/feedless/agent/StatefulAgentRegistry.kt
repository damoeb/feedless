package org.migor.feedless.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.agent.AgentDAO
import org.migor.feedless.data.jpa.agent.AgentEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.agent} & ${AppLayer.repository} & ${AppLayer.service}")
class StatefulAgentRegistry(
  private val agentDAO: AgentDAO
) : AgentRegistry {

  @Transactional(readOnly = true)
  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UUID?): List<AgentEntity> {
    return withContext(Dispatchers.IO) {
      agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
    }
  }

  @Transactional(readOnly = true)
  override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID): AgentEntity? {
    return withContext(Dispatchers.IO) {
      agentDAO.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId)
    }
  }

  @Transactional
  override suspend fun delete(agent: AgentEntity) {
    withContext(Dispatchers.IO) {
      agentDAO.delete(agent)
    }
  }

  @Transactional
  override suspend fun save(agent: AgentEntity) {
    withContext(Dispatchers.IO) {
      agentDAO.save(agent)
    }
  }
}
