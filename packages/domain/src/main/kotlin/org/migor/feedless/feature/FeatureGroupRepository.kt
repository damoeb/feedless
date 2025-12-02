package org.migor.feedless.feature

import java.util.*

interface FeatureGroupRepository {

  suspend fun findByParentFeatureGroupIdIsNull(): FeatureGroup?
  suspend fun findByNameEqualsIgnoreCase(name: String): FeatureGroup?
  suspend fun findById(id: FeatureGroupId): Optional<FeatureGroup>
  suspend fun findAll(): List<FeatureGroup>
  suspend fun save(featureGroup: FeatureGroup): FeatureGroup
  suspend fun deleteById(id: FeatureGroupId)
}
