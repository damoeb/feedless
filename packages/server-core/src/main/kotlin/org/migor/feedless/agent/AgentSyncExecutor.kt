package org.migor.feedless.agent

import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.agent} & ${AppLayer.scheduler}")
class AgentSyncExecutor(
  private val agentService: AgentService,
  private val agentDAO: AgentRepository
) {

  @Scheduled(fixedDelay = 2 * 60 * 1000, initialDelay = 5000)
  @Transactional
  fun executeSync() {
    runBlocking {
      agentDAO.saveAll(
        agentService.agentRefs().mapNotNull {
          agentDAO.findByConnectionIdAndSecretKeyId(it.connectionId, it.secretKeyId)
        }.map {
          it.copy(lastSyncedAt = LocalDateTime.now())
        })
    }
  }

  @Scheduled(fixedDelay = 3 * 60 * 1000, initialDelay = 5000)
  @Transactional
  fun executeCleanup() {
    runBlocking {
      agentDAO.deleteAllByLastSyncedAtBefore(
        LocalDateTime.now().minusMinutes(2)
      )
    }
  }
}
