package org.migor.feedless.data.jpa.browserautomation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppLayer.repository} & ${AppProfiles.browserAutomation}")
interface BrowserAutomationDAO : JpaRepository<BrowserAutomationEntity, UUID> {
  fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UUID?): List<BrowserAutomationEntity>
  fun deleteAllByLastSyncedAtBefore(date: LocalDateTime)
  fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID): BrowserAutomationEntity?
}
