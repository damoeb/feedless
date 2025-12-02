package org.migor.feedless.data.jpa.feature

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.Feature
import org.migor.feedless.feature.FeatureId
import org.migor.feedless.feature.FeatureRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
class FeatureJpaRepository(private val featureDAO: FeatureDAO) : FeatureRepository {
  override suspend fun findByName(name: String): Feature? {
    return withContext(Dispatchers.IO) {
      featureDAO.findByName(name)?.toDomain()
    }
  }

  override suspend fun save(feature: Feature): Feature {
    return withContext(Dispatchers.IO) {
      featureDAO.save(feature.toEntity()).toDomain()
    }
  }

  override suspend fun findById(featureId: FeatureId): Feature? {
    return withContext(Dispatchers.IO) {
      featureDAO.findById(featureId.uuid).getOrNull()?.toDomain()
    }
  }

}
