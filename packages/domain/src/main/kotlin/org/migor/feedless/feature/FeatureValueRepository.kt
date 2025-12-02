package org.migor.feedless.feature

interface FeatureValueRepository {

  suspend fun findByFeatureGroupIdAndFeatureId(featureGroupId: FeatureGroupId, featureId: FeatureId): FeatureValue?

  suspend fun resolveByFeatureGroupIdAndName(
    featureGroupId: FeatureGroupId,
    feature: String
  ): FeatureValue?


  suspend fun resolveAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue>
  suspend fun findAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue>
  suspend fun deleteAllByFeatureGroupId(id: FeatureGroupId)
  suspend fun findById(id: FeatureValueId): FeatureValue?
  suspend fun save(feature: FeatureValue): FeatureValue

}
