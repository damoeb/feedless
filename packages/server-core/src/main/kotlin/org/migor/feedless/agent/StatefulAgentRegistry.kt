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

  /**
   * Every row in `t_agent`, so the count is cluster-wide: each pod writes a row for every agent
   * connected to it and deletes that row as soon as the agent disconnects (`AgentService.removeAgent`).
   * Only a pod that dies leaves its rows behind: `AgentSyncExecutor` refreshes rows every 2 minutes
   * and, every 3 minutes, deletes rows not refreshed for 2 minutes, so a dead pod's agents can still
   * count for up to about 5 minutes.
   */
  override suspend fun countConnected(): Int = withContext(Dispatchers.IO) {
    agentRepository.count().toInt()
  }
}
