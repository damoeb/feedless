package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface FeatureDAO : JpaRepository<FeatureEntity, UUID> {
  fun findAllByPlanId(id: UUID): List<FeatureEntity>

  fun findByPlanIdAndName(planId: UUID, featureName: FeatureName): FeatureEntity?
  fun existsByPlanIdAndName(planId: UUID, featureName: FeatureName): Boolean
  fun findFirstByName(featureName: FeatureName): FeatureEntity
}
