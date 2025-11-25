package org.migor.feedless.data.jpa.featureValue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.feature.FeatureId
import org.migor.feedless.feature.FeatureValue
import org.migor.feedless.feature.FeatureValueRepository
import org.migor.feedless.plan.PlanId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
class FeatureValueJpaRepository(private val featureValueDAO: FeatureValueDAO) : FeatureValueRepository {
  override suspend fun findByFeatureGroupIdAndFeatureId(
    planId: PlanId,
    featureId: FeatureId
  ): FeatureValue? {
    return withContext(Dispatchers.IO) {
      featureValueDAO.findByFeatureGroupIdAndFeatureId(planId.uuid, featureId.uuid)?.toDomain()
    }
  }

  override suspend fun resolveByFeatureGroupIdAndName(
    featureGroupId: FeatureGroupId,
    feature: String
  ): FeatureValue? {
    return withContext(Dispatchers.IO) {
      featureValueDAO.resolveByFeatureGroupIdAndName(featureGroupId.uuid, feature)?.toDomain()
    }
  }

  override suspend fun resolveAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue> {
    return withContext(Dispatchers.IO) {
      featureValueDAO.resolveAllByFeatureGroupId(featureGroupId.uuid).map { it.toDomain() }
    }
  }

  override suspend fun findAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue> {
    return withContext(Dispatchers.IO) {
      featureValueDAO.findAllByFeatureGroupId(featureGroupId.uuid).map { it.toDomain() }
    }
  }

  override suspend fun deleteAllByFeatureGroupId(id: FeatureGroupId) {
    withContext(Dispatchers.IO) {
      featureValueDAO.deleteAllByFeatureGroupId(id.uuid)
    }
  }


}
