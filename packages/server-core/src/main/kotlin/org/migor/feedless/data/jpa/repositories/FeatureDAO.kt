package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.data.jpa.models.FeatureEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeatureDAO : JpaRepository<FeatureEntity, UUID> {
  fun findAllByPlanId(id: UUID): List<FeatureEntity>
}
