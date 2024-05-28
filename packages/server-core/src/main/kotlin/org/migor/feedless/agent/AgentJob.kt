package org.migor.feedless.agent

import org.migor.feedless.AppProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.agent} & ${AppProfiles.cron}")
class AgentJob {

  @Autowired
  private lateinit var agentService: AgentService

  @Autowired
  private lateinit var agentDAO: AgentDAO

  @Scheduled(fixedDelay = 2 * 60 * 1000, initialDelay = 5000)
  @Transactional
  fun executeSync() {
    agentDAO.saveAll(
      agentService.agentRefs().mapNotNull {
        agentDAO.findByConnectionIdAndSecretKeyId(it.connectionId, it.secretKeyId)
      }.map {
        it.lastSyncedAt = Date()
        it
      })
  }

  @Scheduled(fixedDelay = 3 * 60 * 1000, initialDelay = 5000)
  @Transactional
  fun executeCleanup() {
    agentDAO.deleteAllByLastSyncedAtBefore(
      Date.from(
        LocalDateTime.now().minusMinutes(2).atZone(ZoneId.systemDefault()).toInstant()
      )
    )
  }
}
