package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValueInput
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.FeatureGroupWhereInput
import org.migor.feedless.generated.types.FeatureIntValueInput
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto

@Service
@Profile(AppProfiles.database)
class FeatureService {

  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var productDAO: ProductDAO

  @Autowired
  private lateinit var featureDAO: FeatureDAO

  @Autowired
  private lateinit var featureGroupDAO: FeatureGroupDAO

  @Autowired
  private lateinit var featureValueDAO: FeatureValueDAO

  fun isDisabled(featureName: FeatureName, featureGroupIdOptional: UUID? = null): Boolean {
    val featureGroupId = featureGroupIdOptional ?: featureGroupDAO.findByParentFeatureGroupIdIsNull()!!.id

    return featureValueDAO.resolveByFeatureGroupIdAndName(featureGroupId, featureName.name)?.let { feature ->
      run {
        assert(feature.valueType == FeatureValueType.bool)
        !feature.valueBoolean!!
      }
    } ?: false
  }

  @Transactional(readOnly = true)
  fun findAllByProduct(product: ProductCategory): List<Feature> {
    val featureGroup = productDAO.findByPartOfAndBaseProductIsTrue(product)?.featureGroup
      ?: featureGroupDAO.findByParentFeatureGroupIdIsNull()!!
    val productFeatures = featureValueDAO.resolveAllByFeatureGroupId(featureGroup.id)
    return toDTO(productFeatures)
  }

  private fun toDTO(values: List<FeatureValueEntity>): List<Feature> {
    return values.mapNotNull { value ->
      value.feature!!.toDto()?.let {
        Feature.newBuilder()
          .id(value.feature!!.id.toString())
          .name(value.feature!!.toDto())
          .value(value.toDto())
          .build()
      }
    }

  }

  fun updateFeatureValue(
    corrId: String,
    id: UUID,
    intValue: FeatureIntValueInput?,
    boolValue: FeatureBooleanValueInput?,
    productId: UUID? = null
  ) {
    if (!sessionService.user(corrId).root) {
      throw IllegalArgumentException("must be root")
    }
    val feature = featureValueDAO.findById(id).orElseThrow()

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

  fun assignFeatureValues(
    featureGroup: FeatureGroupEntity,
    features: Map<FeatureName, FeatureValueEntity>
  ) {
    features.forEach { (featureName, nextFeatureValue) ->
      run {
        val feature = featureDAO.findByName(featureName.name) ?: createFeature(featureName)

        featureValueDAO.findByFeatureGroupIdAndFeatureId(featureGroup.id, feature.id)
          ?.let { currentFeatureValue ->
            val logValueUpdate =
              { current: Any?, next: Any? -> log.warn("Patching value feature $featureName for ${featureGroup.name} $current -> $next") }
            if (currentFeatureValue.valueBoolean != nextFeatureValue.valueBoolean) {
              logValueUpdate(currentFeatureValue.valueBoolean, nextFeatureValue.valueBoolean)
              currentFeatureValue.valueBoolean = nextFeatureValue.valueBoolean
            }
            if (currentFeatureValue.valueInt != nextFeatureValue.valueInt) {
              logValueUpdate(currentFeatureValue.valueInt, nextFeatureValue.valueInt)
              currentFeatureValue.valueInt = nextFeatureValue.valueInt
            }
            featureValueDAO.save(currentFeatureValue)
          }
          ?: run {
            val value = FeatureValueEntity()
            value.featureGroupId = featureGroup.id
            value.featureId = feature.id
            value.valueType = nextFeatureValue.valueType
            value.valueBoolean = nextFeatureValue.valueBoolean
            value.valueInt = nextFeatureValue.valueInt
            featureValueDAO.save(value)
          }
      }
    }
  }

  private fun createFeature(featureName: FeatureName): FeatureEntity {
    val feature = FeatureEntity()
    feature.name = featureName.name
    return featureDAO.save(feature)
  }


  fun findAllGroups(inherit: Boolean, where: FeatureGroupWhereInput): List<FeatureGroup> {
    val groups = if (where.id == null) {
      this.featureGroupDAO.findAll()
    } else {
      listOf(this.featureGroupDAO.findById(UUID.fromString(where.id!!.equals)).orElseThrow())
    }
    return groups.map { it.toDto(findAllByGroupId(it.id, inherit)) }
  }

  private fun findAllByGroupId(featureGroupId: UUID, inherit: Boolean): List<Feature> {
    return toDTO(
      if (inherit) {
        featureValueDAO.resolveAllByFeatureGroupId(featureGroupId)
      } else {
        featureValueDAO.findAllByFeatureGroupId(featureGroupId)
      }
    )
  }
}

val mapFeatureName2Dto = mapOf(
  FeatureName.canJoinPlanWaitList to FeatureNameDto.canJoinPlanWaitList,
  FeatureName.canActivatePlan to FeatureNameDto.canActivatePlan,

  FeatureName.requestPerMinuteUpperLimitInt to FeatureNameDto.requestPerMinuteUpperLimitInt,
  FeatureName.refreshRateInMinutesLowerLimitInt to FeatureNameDto.refreshRateInMinutesLowerLimit,
  FeatureName.publicRepositoryBool to FeatureNameDto.publicRepository,

  FeatureName.scrapeRequestTimeoutMsecInt to FeatureNameDto.scrapeRequestTimeoutMsec,
  FeatureName.repositoryRetentionMaxDaysLowerLimitInt to FeatureNameDto.repositoryRetentionMaxDaysLowerLimitInt,
  FeatureName.repositoryCapacityUpperLimitInt to FeatureNameDto.repositoryCapacityUpperLimitInt,

  FeatureName.pluginsBool to FeatureNameDto.plugins,
  FeatureName.scrapeSourceMaxCountActiveInt to FeatureNameDto.scrapeSourceMaxCountActive,
  FeatureName.scrapeSourceMaxCountTotalInt to FeatureNameDto.scrapeSourceMaxCountTotal,
  FeatureName.sourceMaxCountPerRepositoryInt to FeatureNameDto.scrapeRequestMaxCountPerSource,
)

private fun FeatureEntity.toDto(): FeatureNameDto? {
  return try {
    val featureName = FeatureName.valueOf(name)
    mapFeatureName2Dto[featureName]
  } catch (e: Exception) {
    null
  }
}


