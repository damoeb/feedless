package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValueInput
import org.migor.feedless.generated.types.FeatureIntValueInput
import org.migor.feedless.session.SessionService
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
  private lateinit var sessionService: SessionService

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

  fun updateFeature(
    corrId: String,
    name: org.migor.feedless.generated.types.FeatureName,
    intValue: FeatureIntValueInput?,
    boolValue: FeatureBooleanValueInput?
  ) {
    if (!sessionService.user(corrId).root) {
      throw IllegalArgumentException("must be root")
    }
    val feature = featureValueDAO.findByProductNameAndFeatureName(ProductName.system.name, name.fromDto()!!.name)
    if (feature.valueType == FeatureValueType.bool) {
      feature.valueBoolean = boolValue!!.value
      featureValueDAO.save(feature)
    } else {
      if (feature.valueType == FeatureValueType.number) {
        feature.valueInt = intValue!!.value
        featureValueDAO.save(feature)
      } else {
        throw IllegalArgumentException("Value type ${feature.valueType} is not supported")
      }
    }
  }
}

val mapFeatureName2Dto = mapOf(
  FeatureName.canCreateUser to FeatureNameDto.canCreateUser,
  FeatureName.hasWaitList to FeatureNameDto.hasWaitList,
  FeatureName.canSignUp to FeatureNameDto.canSignUp,
  FeatureName.canLogin to FeatureNameDto.canLogin,
  FeatureName.canCreateAsAnonymous to FeatureNameDto.canCreateAsAnonymous,

  FeatureName.rateLimitInt to FeatureNameDto.rateLimit,
  FeatureName.refreshRateInMinutesLowerLimitInt to FeatureNameDto.refreshRateInMinutesLowerLimit,
  FeatureName.publicRepositoryBool to FeatureNameDto.publicRepository,

  FeatureName.scrapeRequestTimeoutInt to FeatureNameDto.scrapeRequestTimeout,
  FeatureName.repositoryRetentionMaxDaysLowerLimitInt to FeatureNameDto.repositoryRetentionMaxDaysLowerLimitInt,
  FeatureName.repositoryRetentionMaxItemsLowerLimitInt to FeatureNameDto.repositoryRetentionMaxItemsLowerLimitInt,
  FeatureName.repositoryRetentionMaxItemsUpperLimitInt to FeatureNameDto.repositoryRetentionMaxItemsUpperLimitInt,

  FeatureName.pluginsBool to FeatureNameDto.plugins,
  FeatureName.scrapeSourceMaxCountActiveInt to FeatureNameDto.scrapeSourceMaxCountActive,
  FeatureName.scrapeSourceMaxCountTotalInt to FeatureNameDto.scrapeSourceMaxCountTotal,
  FeatureName.scrapeRequestMaxCountPerSourceInt to FeatureNameDto.scrapeRequestMaxCountPerSource,
)

private fun FeatureEntity.toDto(): FeatureNameDto? {
  return try {
    val featureName = FeatureName.valueOf(name)
    mapFeatureName2Dto[featureName]
  } catch (e: Exception) {
    null
  }
}

private fun FeatureNameDto.fromDto(): FeatureName? {
  return try {
    val matches = mapFeatureName2Dto.keys.filter { mapFeatureName2Dto[it] == this }
    if (matches.isEmpty()) {
      null
    } else {
      matches[0]
    }
  } catch (e: Exception) {
    null
  }
}



