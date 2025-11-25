package org.migor.feedless.data.jpa.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.agent.Agent
import org.migor.feedless.agent.AgentId
import org.migor.feedless.agent.AgentRepository
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("${AppLayer.repository} & ${AppProfiles.agent}")
class AgentJpaRepository(private val agentDAO: AgentDAO) : AgentRepository {

  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent> {
    return withContext(Dispatchers.IO) {
      agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId?.uuid).map { it.toDomain() }
    }
  }

  override suspend fun deleteAllByLastSyncedAtBefore(date: LocalDateTime) {
    withContext(Dispatchers.IO) {
      agentDAO.deleteAllByLastSyncedAtBefore(date)
    }
  }

  override suspend fun findByConnectionIdAndSecretKeyId(
    connectionId: String,
    secretKeyId: UserSecretId
  ): Agent? {
    return withContext(Dispatchers.IO) {
      agentDAO.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId.uuid)?.toDomain()
    }
  }

  override suspend fun saveAll(agents: List<Agent>): List<Agent> {
    return withContext(Dispatchers.IO) {
      agentDAO.saveAll(agents.map { it.toEntity() }).map { it.toDomain() }
    }
  }

  override suspend fun deleteById(id: AgentId) {
    withContext(Dispatchers.IO) {
      agentDAO.deleteById(id.uuid)
    }
  }

  override suspend fun save(agent: Agent): Agent {
    return withContext(Dispatchers.IO) {
      agentDAO.save(agent.toEntity()).toDomain()
    }
  }

}
