package org.migor.feedless.browserautomation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.browserAutomation} & ${AppLayer.repository} & ${AppLayer.service}")
class StatefulBrowserAutomationRegistry(
  private val browserAutomationRepository: BrowserAutomationRepository
) : BrowserAutomationRegistry {

  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<BrowserAutomation> {
    return withContext(Dispatchers.IO) {
      browserAutomationRepository.findAllByOwnerIdOrOpenInstanceIsTrue(userId)
    }
  }

  override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): BrowserAutomation? =
    withContext(Dispatchers.IO) {
      browserAutomationRepository.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId)
    }

  override suspend fun delete(browserAutomation: BrowserAutomation) = withContext(Dispatchers.IO) {
    browserAutomationRepository.deleteById(browserAutomation.id)
  }

  override suspend fun save(browserAutomation: BrowserAutomation): BrowserAutomation = withContext(Dispatchers.IO) {
    browserAutomationRepository.save(browserAutomation)
  }

  /** Cluster-wide via t_agent; a dead pod's agents can still count for about 5 minutes until BrowserAutomationSyncExecutor sweeps them. */
  override suspend fun countConnected(): Int = withContext(Dispatchers.IO) {
    browserAutomationRepository.count().toInt()
  }
}
