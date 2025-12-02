package org.migor.feedless.feature

import org.migor.feedless.group.GroupId
import java.util.*

interface FeatureGroupRepository {

  fun findByParentFeatureGroupIdIsNull(): FeatureGroup?
  fun findByNameEqualsIgnoreCase(name: String): FeatureGroup?
  fun findById(id: FeatureGroupId): Optional<FeatureGroup>
  fun findAll(): List<FeatureGroup>
  fun save(featureGroup: FeatureGroup): FeatureGroup
  fun deleteById(id: FeatureGroupId)
  fun findByGroupId(groupId: GroupId): FeatureGroup?
}
