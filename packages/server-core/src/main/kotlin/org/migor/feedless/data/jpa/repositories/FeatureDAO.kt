package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureScope
import org.migor.feedless.data.jpa.models.PlanName
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface FeatureDAO : JpaRepository<FeatureEntity, UUID> {
  fun findAllByPlanId(id: UUID): List<FeatureEntity>

  fun findByPlanIdAndName(planId: UUID, featureName: FeatureName): FeatureEntity?
  fun existsByPlanIdAndName(planId: UUID, featureName: FeatureName): Boolean
  fun findFirstByName(featureName: FeatureName): FeatureEntity

  @Query("""
    select f from FeatureEntity f
    inner join PlanEntity p
    on f.planId = p.id
    where p.name=:plan and f.name=:feature
  """)
  fun findByPlanNameAndFeatureName(@Param("plan") plan: PlanName, @Param("feature") feature: FeatureName): FeatureEntity

  @Query("""
    select f from FeatureEntity f
    inner join PlanEntity p
    on f.planId = p.id
    where p.name=:plan and p.product=:product and f.scope=:scope
  """)
  fun findAllByPlanAndProductAndScope(@Param("plan") plan: PlanName, @Param("product") product: ProductName, @Param("scope") scope: FeatureScope): List<FeatureEntity>

}
