package org.migor.feedless.feature

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
interface FeatureGroupDAO : JpaRepository<FeatureGroupEntity, UUID> {

  fun findByParentFeatureGroupIdIsNull(): FeatureGroupEntity?
  fun findByName(name: String): FeatureGroupEntity?
}
