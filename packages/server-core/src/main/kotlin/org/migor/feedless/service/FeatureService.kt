package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureValueType
import org.migor.feedless.data.jpa.repositories.FeatureDAO
import org.migor.feedless.data.jpa.repositories.FeatureValueDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class FeatureService {
  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Autowired
  lateinit var featureValueDAO: FeatureValueDAO

  @Autowired
  lateinit var featureDAO: FeatureDAO

  fun isDisabled(featureName: FeatureName, productName: ProductName): Boolean {
    val feature = featureValueDAO.findByProductNameAndFeatureName(productName.name, featureName.name)
    assert(feature.valueType == FeatureValueType.bool)
    return !feature.valueBoolean!!
  }

  fun findAllByProduct(product: ProductName): List<FeatureEntity> {
    return featureDAO.findByProductName(product.name)
  }

}
