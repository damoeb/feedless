package org.migor.feedless.data.jpa.feature

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.Feature
import org.migor.feedless.feature.FeatureId
import org.migor.feedless.feature.FeatureRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.features} & ${AppLayer.repository}")
class FeatureJpaRepository(private val featureDAO: FeatureDAO) : FeatureRepository {
  override fun findByName(name: String): Feature? {
    return featureDAO.findByName(name)?.toDomain()
  }

  override fun save(feature: Feature): Feature {
    return featureDAO.save(feature.toEntity()).toDomain()
  }

  override fun findById(featureId: FeatureId): Feature? {
    return featureDAO.findById(featureId.uuid).getOrNull()?.toDomain()
  }

}
