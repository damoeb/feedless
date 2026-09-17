package org.migor.feedless.browserautomation

import org.migor.feedless.userSecret.UserSecretId
import org.springframework.stereotype.Service

@Service
interface BrowserAutomationRegistry : BrowserAutomationDirectory {
  suspend fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): BrowserAutomation?
  suspend fun delete(browserAutomation: BrowserAutomation)
  suspend fun save(browserAutomation: BrowserAutomation): BrowserAutomation
}
