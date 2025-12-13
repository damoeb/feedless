package org.migor.feedless.data.jpa.agent

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

  override fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent> {
    return agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId?.uuid).map { it.toDomain() }
  }

  override fun deleteAllByLastSyncedAtBefore(date: LocalDateTime) {
    agentDAO.deleteAllByLastSyncedAtBefore(date)
  }

  override fun findByConnectionIdAndSecretKeyId(
    connectionId: String,
    secretKeyId: UserSecretId
  ): Agent? {
    return agentDAO.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId.uuid)?.toDomain()
  }

  override fun saveAll(agents: List<Agent>): List<Agent> {
    return agentDAO.saveAll(agents.map { it.toEntity() }).map { it.toDomain() }
  }

  override fun deleteById(id: AgentId) {
    agentDAO.deleteById(id.uuid)
  }

  override fun save(agent: Agent): Agent {
    return agentDAO.save(agent.toEntity()).toDomain()
  }

}
