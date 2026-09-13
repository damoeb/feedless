package org.migor.feedless.browserautomation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.withMdcCorrId
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.browserAutomation} & ${AppLayer.scheduler}")
class BrowserAutomationSyncExecutor(
  private val browserAutomationService: BrowserAutomationService,
  private val browserAutomationRepository: BrowserAutomationRepository
) {

  @Scheduled(fixedDelay = 2 * 60 * 1000, initialDelay = 5000)
  @Transactional
  fun executeSync() {
    withMdcCorrId {
      browserAutomationRepository.saveAll(
        browserAutomationService.agentRefs().mapNotNull {
          browserAutomationRepository.findByConnectionIdAndSecretKeyId(it.connectionId, it.secretKeyId)
        }.map {
          it.copy(lastSyncedAt = LocalDateTime.now())
        })
    }
  }

  @Scheduled(fixedDelay = 3 * 60 * 1000, initialDelay = 5000)
  @Transactional
  fun executeCleanup() {
    withMdcCorrId {
      browserAutomationRepository.deleteAllByLastSyncedAtBefore(
        LocalDateTime.now().minusMinutes(2)
      )
    }
  }
}
