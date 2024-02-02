package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureScope
import org.migor.feedless.data.jpa.models.FeatureValueType
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.repositories.FeatureDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class FeatureService {
  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Autowired
  lateinit var featureDAO: FeatureDAO

  @Autowired
  lateinit var environment: Environment

  fun withDatabase(): Boolean {
    return environment.acceptsProfiles(Profiles.of(AppProfiles.database))
  }

  fun findAllByPlanId(id: UUID): List<FeatureEntity> {
    return this.featureDAO.findAllByPlanId(id)
  }

  fun isEnabled(name: FeatureName): Boolean {
    val feature = featureDAO.findByPlanNameAndFeatureName(PlanName.system, name)
    assert(feature.valueType == FeatureValueType.bool)
    return feature.valueBoolean!!
  }

  fun isDisabled(name: FeatureName): Boolean {
    return !this.isEnabled(name)
  }

  fun findAllByProduct(productName: ProductName): List<FeatureEntity> {
    return featureDAO.findAllByPlanAndProductAndScope(PlanName.minimal, productName, FeatureScope.frontend)
  }

}
