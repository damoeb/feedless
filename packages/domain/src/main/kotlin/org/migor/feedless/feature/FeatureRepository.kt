package org.migor.feedless.feature

interface FeatureRepository {
  fun findByName(name: String): Feature?
  fun save(feature: Feature): Feature
  fun findById(featureId: FeatureId): Feature?
}
