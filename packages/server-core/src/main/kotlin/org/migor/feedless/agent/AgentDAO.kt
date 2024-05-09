package org.migor.feedless.agent

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface AgentDAO : JpaRepository<AgentEntity, UUID> {
  @Transactional
  fun deleteByConnectionIdAndSecretKeyId(connectionId: String, secretKeyId: UUID)
  fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UUID?): List<AgentEntity>
}
