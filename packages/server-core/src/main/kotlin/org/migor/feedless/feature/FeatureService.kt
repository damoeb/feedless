package org.migor.feedless.feature

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.generated.types.FeatureBooleanValueInput
import org.migor.feedless.generated.types.FeatureGroupWhereInput
import org.migor.feedless.generated.types.FeatureIntValueInput
import org.migor.feedless.plan.PlanRepository
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.user.UserId
import org.migor.feedless.user.isAdmin
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto

@Service
@Profile("${AppProfiles.features} & ${AppLayer.service}")
class FeatureService(
  private val planRepository: PlanRepository,
  private val productRepository: ProductRepository,
  private val featureRepository: FeatureRepository,
  private val featureGroupRepository: FeatureGroupRepository,
  private val featureValueRepository: FeatureValueRepository,
) {

  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  suspend fun isDisabled(featureName: FeatureName, featureGroupIdOptional: FeatureGroupId? = null): Boolean =
    withContext(Dispatchers.IO) {
      val featureGroupId = featureGroupIdOptional ?: featureGroupRepository.findByParentFeatureGroupIdIsNull()!!.id

      featureValueRepository.resolveByFeatureGroupIdAndName(featureGroupId, featureName.name)?.let { feature ->
        run {
          assert(feature.valueType == FeatureValueType.bool)
          !feature.valueBoolean!!
        }
      } ?: false
    }

  suspend fun findAllByProductAndUserId(product: Vertical, userId: UserId): List<FeatureValue> =
    withContext(Dispatchers.IO) {
      val plan = planRepository.findActiveByUserAndProductIn(userId, listOf(product), LocalDateTime.now())
      plan?.let {
        featureValueRepository.resolveAllByFeatureGroupId(plan.product(productRepository)!!.featureGroupId!!)
      } ?: emptyList()
    }

//  private fun toDTO(values: List<FeatureValue>): List<Feature> {
//    return values.mapNotNull { value ->
//      value.feature!!.toDto()?.let {
//        Feature(
//          id = value.feature!!.id.toString(),
//          name = value.feature!!.toDto()!!,
//          value = value.toDto(),
//        )
//      }
//    }
//  }

  suspend fun updateFeatureValue(
    id: FeatureValueId,
    intValue: FeatureIntValueInput?,
    boolValue: FeatureBooleanValueInput?,
    productId: ProductId? = null
  ) {

    if (!isAdmin()) {
      throw IllegalArgumentException("must be root")
    }

    val feature = featureValueRepository.findById(id)!!

    if (feature.valueType == FeatureValueType.bool) {
      featureValueRepository.save(
        feature.copy(
          valueBoolean = boolValue!!.value
        )
      )
    } else {
      if (feature.valueType == FeatureValueType.number) {
        featureValueRepository.save(
          feature.copy(
            valueInt = intValue!!.value
          )
        )
      } else {
        throw IllegalArgumentException("Value type ${feature.valueType} is not supported")
      }
    }
  }

  private suspend fun isAdmin(): Boolean {
    return currentCoroutineContext().isAdmin()
  }

  @Transactional(propagation = Propagation.MANDATORY)
  fun assignFeatureValues(
    featureGroup: FeatureGroup,
    features: Map<FeatureName, FeatureValue>, resetFeatureGroup: Boolean = true
  ) {

    if (resetFeatureGroup) {
      featureValueRepository.deleteAllByFeatureGroupId(featureGroup.id)
    }

    features.forEach { (featureName, nextFeatureValue) ->
      val feature = featureRepository.findByName(featureName.name) ?: createFeature(featureName)

      featureValueRepository.findByFeatureGroupIdAndFeatureId(featureGroup.id, feature.id)
        ?.let { currentFeatureValue ->
          val logValueUpdate =
            { current: Any?, next: Any? -> log.warn("Patching value feature $featureName for ${featureGroup.name} $current -> $next") }
          if (currentFeatureValue.valueBoolean != nextFeatureValue.valueBoolean) {
            logValueUpdate(currentFeatureValue.valueBoolean, nextFeatureValue.valueBoolean)

            featureValueRepository.save(currentFeatureValue.copy(valueBoolean = nextFeatureValue.valueBoolean))
          }
          if (currentFeatureValue.valueInt != nextFeatureValue.valueInt) {
            logValueUpdate(currentFeatureValue.valueInt, nextFeatureValue.valueInt)

            featureValueRepository.save(currentFeatureValue.copy(valueInt = nextFeatureValue.valueInt))
          }
        }
        ?: featureValueRepository.save(
          FeatureValue(
            featureGroupId = featureGroup.id,
            featureId = feature.id,
            valueType = nextFeatureValue.valueType,
            valueBoolean = nextFeatureValue.valueBoolean,
            valueInt = nextFeatureValue.valueInt
          )
        )
    }
  }

  private fun createFeature(featureName: FeatureName): Feature {
    val feature = Feature(
      name = featureName.name
    )
    return featureRepository.save(feature)
  }

  suspend fun findAllGroups(inherit: Boolean, where: FeatureGroupWhereInput): List<FeatureGroup> {
    val groups = if (where.id == null) {
      // todo inherit not used
      featureGroupRepository.findAll()
    } else {
      listOf(featureGroupRepository.findById(FeatureGroupId(where.id!!.eq!!)).orElseThrow())
    }

    return groups
  }

  suspend fun findAllByGroupId(featureGroupId: FeatureGroupId, inherit: Boolean): List<FeatureValue> {
    return if (inherit) {
      featureValueRepository.resolveAllByFeatureGroupId(featureGroupId)
    } else {
      featureValueRepository.findAllByFeatureGroupId(featureGroupId)
    }
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
  FeatureName.repositoriesMaxCountTotalInt to FeatureNameDto.repositoriesMaxCountTotalInt,
  FeatureName.sourceMaxCountPerRepositoryInt to FeatureNameDto.sourceMaxCountPerRepositoryInt,

  FeatureName.pluginsBool to FeatureNameDto.plugins,
//  FeatureName.repositoriesMaxCountActiveInt to FeatureNameDto.scrapeSourceMaxCountActive,
//  FeatureName.repositoriesMaxCountTotalInt to FeatureNameDto.scrapeSourceMaxCountTotal,
//  FeatureName.sourceMaxCountPerRepositoryInt to FeatureNameDto.scrapeRequestMaxCountPerSource,
)

//private fun FeatureGroup.toDto(): FeatureGroupDto? {
//  return try {
////    val featureName = FeatureName.valueOf(name)
////    mapFeatureName2Dto[featureName]
//  } catch (e: Exception) {
//    null
//  }
//}

