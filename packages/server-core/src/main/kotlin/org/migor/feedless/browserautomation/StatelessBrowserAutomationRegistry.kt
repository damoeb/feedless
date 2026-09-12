package org.migor.feedless.browserautomation

import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.browserAutomation)
@ConditionalOnMissingBean(StatefulBrowserAutomationRegistry::class)
class StatelessBrowserAutomationRegistry : BrowserAutomationRegistry {

  private val registry = mutableListOf<BrowserAutomation>()

  override suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<BrowserAutomation> {
    // Same semantics as the JPA query of the same name: the caller's own agents, plus every open one.
    return registry.filter { it.ownerId == userId || it.openInstance }
  }

  override suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): BrowserAutomation? {
    return registry.find { it.connectionId == connectionId && it.secretKeyId == secretKeyId }
  }

  override suspend fun delete(browserAutomation: BrowserAutomation) {
    registry.remove(browserAutomation)
  }

  override suspend fun save(browserAutomation: BrowserAutomation): BrowserAutomation {
    registry.add(browserAutomation)
    return browserAutomation
  }

  override suspend fun countConnected(): Int = registry.size
}
