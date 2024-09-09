package org.migor.feedless.agent

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface AgentDAO : JpaRepository<AgentEntity, UUID> {
  fun deleteByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID)
  fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UUID?): List<AgentEntity>
  fun deleteAllByLastSyncedAtBefore(date: LocalDateTime)

  fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID): AgentEntity?
}
