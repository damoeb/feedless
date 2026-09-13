package org.migor.feedless.data.jpa.browserautomation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.browserautomation.BrowserAutomation
import org.migor.feedless.browserautomation.BrowserAutomationId
import org.migor.feedless.browserautomation.BrowserAutomationRepository
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("${AppLayer.repository} & ${AppProfiles.browserAutomation}")
class BrowserAutomationJpaRepository(private val browserAutomationDAO: BrowserAutomationDAO) : BrowserAutomationRepository {

  override fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<BrowserAutomation> {
    return browserAutomationDAO.findAllByOwnerIdOrOpenInstanceIsTrue(userId?.uuid).map { it.toDomain() }
  }

  override fun deleteAllByLastSyncedAtBefore(date: LocalDateTime) {
    browserAutomationDAO.deleteAllByLastSyncedAtBefore(date)
  }

  override fun findByConnectionIdAndSecretKeyId(
    connectionId: String,
    secretKeyId: UserSecretId
  ): BrowserAutomation? {
    return browserAutomationDAO.findByConnectionIdAndSecretKeyId(connectionId, secretKeyId.uuid)?.toDomain()
  }

  override fun saveAll(agents: List<BrowserAutomation>): List<BrowserAutomation> {
    return browserAutomationDAO.saveAll(agents.map { it.toEntity() }).map { it.toDomain() }
  }

  override fun deleteById(id: BrowserAutomationId) {
    browserAutomationDAO.deleteById(id.uuid)
  }

  override fun save(browserAutomation: BrowserAutomation): BrowserAutomation {
    return browserAutomationDAO.save(browserAutomation.toEntity()).toDomain()
  }

  override fun count(): Long {
    return browserAutomationDAO.count()
  }

}
