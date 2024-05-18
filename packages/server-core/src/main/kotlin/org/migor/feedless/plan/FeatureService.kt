package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.generated.types.Feature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto

@Service
@Profile(AppProfiles.database)
class FeatureService {
  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Autowired
  lateinit var featureValueDAO: FeatureValueDAO

  fun isDisabled(featureName: FeatureName, productName: ProductName): Boolean {
    val feature = featureValueDAO.findByProductNameAndFeatureName(productName.name, featureName.name)
    assert(feature.valueType == FeatureValueType.bool)
    return !feature.valueBoolean!!
  }

  @Transactional(readOnly = true)
  fun findAllByProduct(product: ProductName): List<Feature> {
    val productFeatures = featureValueDAO.findAllByProductName(ProductName.system.name)
    val systemFeatures = featureValueDAO.findAllByProductName(product.name)
      .filter { feature -> productFeatures.none { otherFeature -> otherFeature.featureId == feature.featureId } }
    return productFeatures.plus(systemFeatures).mapNotNull { feature ->
      feature.feature!!.toDto()?.let {
        Feature.newBuilder()
          .name(feature.feature!!.toDto())
          .value(feature.toDto())
          .build()
      }
    }
  }
}

private fun FeatureEntity.toDto(): FeatureNameDto? {
  return try {
    when(FeatureName.valueOf(name)) {
      FeatureName.canCreateUser -> FeatureNameDto.canCreateUser
      FeatureName.hasWaitList -> FeatureNameDto.hasWaitList
      FeatureName.canSignUp -> FeatureNameDto.canSignUp
      FeatureName.canLogin -> FeatureNameDto.canLogin
      FeatureName.canCreateAsAnonymous -> FeatureNameDto.canCreateAsAnonymous

      FeatureName.rateLimitInt -> FeatureNameDto.rateLimit
      FeatureName.refreshRateInMinutesLowerLimitInt -> FeatureNameDto.refreshRateInMinutesLowerLimit
      FeatureName.publicRepositoryBool -> FeatureNameDto.publicRepository

      FeatureName.scrapeRequestTimeoutInt -> FeatureNameDto.scrapeRequestTimeout
      FeatureName.repositoryRetentionMaxDaysLowerLimitInt -> FeatureNameDto.repositoryRetentionMaxDaysLowerLimitInt
      FeatureName.repositoryRetentionMaxItemsLowerLimitInt -> FeatureNameDto.repositoryRetentionMaxItemsLowerLimitInt
      FeatureName.repositoryRetentionMaxItemsUpperLimitInt -> FeatureNameDto.repositoryRetentionMaxItemsUpperLimitInt

      FeatureName.pluginsBool -> FeatureNameDto.plugins
      FeatureName.scrapeSourceMaxCountActiveInt -> FeatureNameDto.scrapeSourceMaxCountActive
      FeatureName.scrapeSourceMaxCountTotalInt -> FeatureNameDto.scrapeSourceMaxCountTotal
      FeatureName.scrapeRequestMaxCountPerSourceInt -> FeatureNameDto.scrapeRequestMaxCountPerSource
      else -> null
    }
  } catch (e: Exception) {
    null
  }
}



