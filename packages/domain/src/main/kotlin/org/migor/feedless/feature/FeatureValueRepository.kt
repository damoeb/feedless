package org.migor.feedless.feature

interface FeatureValueRepository {

  fun findByFeatureGroupIdAndFeatureId(featureGroupId: FeatureGroupId, featureId: FeatureId): FeatureValue?

  fun resolveByFeatureGroupIdAndName(
    featureGroupId: FeatureGroupId,
    feature: String
  ): FeatureValue?


  fun resolveAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue>
  fun findAllByFeatureGroupId(featureGroupId: FeatureGroupId): List<FeatureValue>
  fun deleteAllByFeatureGroupId(id: FeatureGroupId)
  fun findById(id: FeatureValueId): FeatureValue?
  fun save(feature: FeatureValue): FeatureValue

}
