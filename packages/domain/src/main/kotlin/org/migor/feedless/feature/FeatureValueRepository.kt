package org.migor.feedless.feature

import org.migor.feedless.plan.PlanId

interface FeatureValueRepository {

  suspend fun findByFeatureGroupIdAndFeatureId(planId: PlanId, featureId: FeatureId): FeatureValue?

  suspend fun resolveByFeatureGroupIdAndName(
    featureGroupId: FeatureGroupId,
    feature: String
  ): FeatureValue?


  suspend fun resolveAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue>
  suspend fun findAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue>
  suspend fun deleteAllByFeatureGroupId(id: FeatureGroupId)

}
