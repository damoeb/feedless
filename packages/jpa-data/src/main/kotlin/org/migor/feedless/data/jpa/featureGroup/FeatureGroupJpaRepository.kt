package org.migor.feedless.data.jpa.featureGroup

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.FeatureGroup
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.feature.FeatureGroupRepository
import org.migor.feedless.group.GroupId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*

@Component
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
class FeatureGroupJpaRepository(private val featureGroupDAO: FeatureGroupDAO) : FeatureGroupRepository {
  override fun findByParentFeatureGroupIdIsNull(): FeatureGroup? {
    return featureGroupDAO.findByParentFeatureGroupIdIsNull()?.toDomain()
  }

  override fun findByNameEqualsIgnoreCase(name: String): FeatureGroup? {
    return featureGroupDAO.findByNameEqualsIgnoreCase(name)?.toDomain()
  }

  override fun findById(id: FeatureGroupId): Optional<FeatureGroup> {
    return featureGroupDAO.findById(id.uuid).map { it.toDomain() }
  }

  override fun findAll(): List<FeatureGroup> {
    return featureGroupDAO.findAll().map { it.toDomain() }
  }

  override fun save(featureGroup: FeatureGroup): FeatureGroup {
    return featureGroupDAO.save(featureGroup.toEntity()).toDomain()
  }

  override fun deleteById(id: FeatureGroupId) {
    featureGroupDAO.deleteById(id.uuid)
  }

  override fun findByGroupId(groupId: GroupId): FeatureGroup? {
    TODO("Not yet implemented")
//    return featureGroupDAO.findByGroupId(groupId.uuid)?.toDomain()
  }

}
