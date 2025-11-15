package org.migor.feedless.feature

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.api.toDto
import org.migor.feedless.data.jpa.feature.FeatureDAO
import org.migor.feedless.data.jpa.feature.FeatureEntity
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupDAO
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupEntity
import org.migor.feedless.data.jpa.featureValue.FeatureName
import org.migor.feedless.data.jpa.featureValue.FeatureValueDAO
import org.migor.feedless.data.jpa.featureValue.FeatureValueEntity
import org.migor.feedless.data.jpa.plan.PlanDAO
import org.migor.feedless.data.jpa.product.ProductDAO
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValueInput
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.FeatureGroupWhereInput
import org.migor.feedless.generated.types.FeatureIntValueInput
import org.migor.feedless.plan.ProductId
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto

data class FeatureGroupId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}

data class FeatureValueId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.features} & ${AppLayer.service}")
class FeatureService(
  private val sessionService: SessionService,
  private val productDAO: ProductDAO,
  private val planDAO: PlanDAO,
  private val featureDAO: FeatureDAO,
  private val featureGroupDAO: FeatureGroupDAO,
  private val featureValueDAO: FeatureValueDAO
) {

  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Transactional(readOnly = true)
  suspend fun isDisabled(featureName: FeatureName, featureGroupIdOptional: FeatureGroupId? = null): Boolean {
    return withContext(Dispatchers.IO) {
      val featureGroupId = featureGroupIdOptional?.value ?: featureGroupDAO.findByParentFeatureGroupIdIsNull()!!.id

      featureValueDAO.resolveByFeatureGroupIdAndName(featureGroupId, featureName.name)?.let { feature ->
        run {
          assert(feature.valueType == FeatureValueType.bool)
          !feature.valueBoolean!!
        }
      } ?: false
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByProductAndUserId(product: Vertical, userId: UserId): List<Feature> {
    return withContext(Dispatchers.IO) {
      val plan = planDAO.findActiveByUserAndProductIn(userId.value, listOf(product), LocalDateTime.now())
      plan?.let {
        val productFeatures = featureValueDAO.resolveAllByFeatureGroupId(plan.product!!.featureGroupId)
        toDTO(productFeatures)
      } ?: emptyList()
    }
  }

  private fun toDTO(values: List<FeatureValueEntity>): List<Feature> {
    return values.mapNotNull { value ->
      value.feature!!.toDto()?.let {
        Feature(
          id = value.feature!!.id.toString(),
          name = value.feature!!.toDto()!!,
          value = value.toDto(),
        )
      }
    }
  }

  @Transactional
  suspend fun updateFeatureValue(
    id: FeatureValueId,
    intValue: FeatureIntValueInput?,
    boolValue: FeatureBooleanValueInput?,
    productId: ProductId? = null
  ) {
    if (!sessionService.user().admin) {
      throw IllegalArgumentException("must be root")
    }

    withContext(Dispatchers.IO) {
      val feature = featureValueDAO.findById(id.value).orElseThrow()

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

  @Transactional
  fun assignFeatureValues(
    featureGroup: FeatureGroupEntity,
    features: Map<FeatureName, FeatureValueEntity>, resetFeatureGroup: Boolean = true
  ) {

    if (resetFeatureGroup) {
      featureValueDAO.deleteAllByFeatureGroupId(featureGroup.id)
    }

    features.forEach { (featureName, nextFeatureValue) ->
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

  private fun createFeature(featureName: FeatureName): FeatureEntity {
    val feature = FeatureEntity()
    feature.name = featureName.name
    return featureDAO.save(feature)
  }

  @Transactional(readOnly = true)
  suspend fun findAllGroups(inherit: Boolean, where: FeatureGroupWhereInput): List<FeatureGroup> {
    val groups = withContext(Dispatchers.IO) {
      if (where.id == null) {
        featureGroupDAO.findAll()
      } else {
        listOf(featureGroupDAO.findById(UUID.fromString(where.id!!.eq)).orElseThrow())
      }
    }
    return groups.map { it.toDto(findAllByGroupId(FeatureGroupId(it.id), inherit)) }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByGroupId(featureGroupId: FeatureGroupId, inherit: Boolean): List<Feature> {
    return withContext(Dispatchers.IO) {
      toDTO(
        if (inherit) {
          featureValueDAO.resolveAllByFeatureGroupId(featureGroupId.value)
        } else {
          featureValueDAO.findAllByFeatureGroupId(featureGroupId.value)
        }
      )
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

private fun FeatureEntity.toDto(): FeatureNameDto? {
  return try {
    val featureName = FeatureName.valueOf(name)
    mapFeatureName2Dto[featureName]
  } catch (e: Exception) {
    null
  }
}


