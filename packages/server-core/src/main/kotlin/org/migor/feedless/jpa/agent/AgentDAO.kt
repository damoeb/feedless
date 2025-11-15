package org.migor.feedless.jpa.agent

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppLayer.repository} & ${AppProfiles.agent}")
interface AgentDAO : JpaRepository<AgentEntity, UUID> {
  fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UUID?): List<AgentEntity>
  fun deleteAllByLastSyncedAtBefore(date: LocalDateTime)
  fun findByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID): AgentEntity?
}
