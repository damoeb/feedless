package org.migor.feedless.data.jpa.featureGroup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.FeatureGroup
import org.migor.feedless.feature.FeatureGroupRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
class FeatureGroupJpaRepository(private val featureGroupDAO: FeatureGroupDAO) : FeatureGroupRepository {
  override suspend fun findByParentFeatureGroupIdIsNull(): FeatureGroup? {
    return withContext(Dispatchers.IO) {
      featureGroupDAO.findByParentFeatureGroupIdIsNull()?.toDomain()
    }
  }

  override suspend fun findByNameEqualsIgnoreCase(name: String): FeatureGroup? {
    return withContext(Dispatchers.IO) {
      featureGroupDAO.findByNameEqualsIgnoreCase(name)?.toDomain()
    }
  }

}
