package org.migor.feedless.data.jpa.featureValue

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.feature.FeatureId
import org.migor.feedless.feature.FeatureValue
import org.migor.feedless.feature.FeatureValueId
import org.migor.feedless.feature.FeatureValueRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
class FeatureValueJpaRepository(private val featureValueDAO: FeatureValueDAO) : FeatureValueRepository {
  override fun findByFeatureGroupIdAndFeatureId(
    featureGroupId: FeatureGroupId,
    featureId: FeatureId
  ): FeatureValue? {
    return featureValueDAO.findByFeatureGroupIdAndFeatureId(featureGroupId.uuid, featureId.uuid)?.toDomain()
  }

  override fun resolveByFeatureGroupIdAndName(
    featureGroupId: FeatureGroupId,
    feature: String
  ): FeatureValue? {
    return featureValueDAO.resolveByFeatureGroupIdAndName(featureGroupId.uuid, feature)?.toDomain()
  }

  override fun resolveAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue> {
    return featureValueDAO.resolveAllByFeatureGroupId(featureGroupId.uuid).map { it.toDomain() }
  }

  override fun findAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue> {
    return featureValueDAO.findAllByFeatureGroupId(featureGroupId.uuid).map { it.toDomain() }
  }

  override fun deleteAllByFeatureGroupId(id: FeatureGroupId) {
    featureValueDAO.deleteAllByFeatureGroupId(id.uuid)
  }

  override fun findById(id: FeatureValueId): FeatureValue? {
    return featureValueDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun save(feature: FeatureValue): FeatureValue {
    return featureValueDAO.save(feature.toEntity()).toDomain()
  }

}
