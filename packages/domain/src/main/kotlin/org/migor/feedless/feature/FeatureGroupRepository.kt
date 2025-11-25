package org.migor.feedless.feature

interface FeatureGroupRepository {

  suspend fun findByParentFeatureGroupIdIsNull(): FeatureGroup?
  suspend fun findByNameEqualsIgnoreCase(name: String): FeatureGroup?
}
