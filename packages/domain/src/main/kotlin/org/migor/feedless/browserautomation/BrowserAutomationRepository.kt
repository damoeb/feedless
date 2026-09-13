package org.migor.feedless.browserautomation

import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import java.time.LocalDateTime

interface BrowserAutomationRepository {
  fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<BrowserAutomation>
  fun deleteAllByLastSyncedAtBefore(date: LocalDateTime)
  fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UserSecretId): BrowserAutomation?
  fun saveAll(agents: List<BrowserAutomation>): List<BrowserAutomation>
  fun deleteById(id: BrowserAutomationId)
  fun save(browserAutomation: BrowserAutomation): BrowserAutomation
  fun count(): Long
}
