package org.migor.feedless.agent

import org.migor.feedless.userSecret.UserSecretId
import org.springframework.stereotype.Service

@Service
interface AgentRegistry : AgentDirectory {
  suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent?
  suspend fun delete(agent: Agent)
  suspend fun save(agent: Agent): Agent
}
