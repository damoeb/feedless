package org.migor.feedless.data.jpa.featureGroup

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
interface FeatureGroupDAO : JpaRepository<FeatureGroupEntity, UUID> {

  fun findByParentFeatureGroupIdIsNull(): FeatureGroupEntity?
  fun findByNameEqualsIgnoreCase(name: String): FeatureGroupEntity?

  @Query(
    """select FG from PlanEntity PL
    inner join ProductEntity PR on PR.id = PL.productId
    inner join FeatureGroupEntity FG ON FG.id = PR.featureGroupId
    WHERE PL.groupId = :groupId"""
  )
  fun findByGroupId(groupId: UUID): FeatureGroupEntity?
}
