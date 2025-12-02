package org.migor.feedless.feature

interface FeatureRepository {
  suspend fun findByName(name: String): Feature?
  suspend fun save(feature: Feature): Feature
  suspend fun findById(featureId: FeatureId): Feature?
}
