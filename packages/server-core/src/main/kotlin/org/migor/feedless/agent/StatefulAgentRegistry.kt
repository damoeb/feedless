package org.migor.feedless.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.agent.AgentDAO
import org.migor.feedless.data.jpa.agent.toDomain
import org.migor.feedless.data.jpa.agent.toEntity
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
    private val agentDAO: AgentDAO
) : AgentRegistry {

    @Transactional(readOnly = true)
    override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent> {
        return withContext(Dispatchers.IO) {
            agentDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId?.uuid).map { it.toDomain() }
        }
    }

    @Transactional(readOnly = true)
    override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): Agent? {
        return withContext(Dispatchers.IO) {
            agentDAO.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId.uuid)?.toDomain()
        }
    }

    @Transactional
    override suspend fun delete(agent: Agent) {
        withContext(Dispatchers.IO) {
            agentDAO.deleteById(agent.id.uuid)
        }
    }

    @Transactional
    override suspend fun save(agent: Agent) {
        withContext(Dispatchers.IO) {
            agentDAO.save(agent.toEntity()).toDomain()
        }
    }
}
