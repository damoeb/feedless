package org.migor.feedless.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.feature.FeatureGroupRepository
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureValue
import org.migor.feedless.feature.FeatureValueRepository
import org.migor.feedless.group.GroupId
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.repository.RepositoryRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class PlanConstraintsService(
  private val planRepository: PlanRepository,
  private val environment: Environment,
  private val repositoryRepository: RepositoryRepository,
  private val featureValueRepository: FeatureValueRepository,
  private val productRepository: ProductRepository,
  private val featureGroupRepository: FeatureGroupRepository,
) {

  private val log = LoggerFactory.getLogger(PlanConstraintsService::class.simpleName)

  fun coerceRetentionMaxCapacity(customMaxItems: Int?, groupId: GroupId): Int? {
    val minItems =
      (getFeatureInt(FeatureName.repositoryCapacityLowerLimitInt, groupId) ?: 0).coerceAtLeast(2).toInt()
    val maxItems = getFeatureInt(FeatureName.repositoryCapacityUpperLimitInt, groupId)?.toInt()
    return customMaxItems?.let {
      maxItems?.let {
        customMaxItems.coerceAtLeast(minItems)
          .coerceAtMost(maxItems)
      } ?: customMaxItems.coerceAtLeast(minItems)
    } ?: maxItems
  }

  fun coerceMinScheduledNextAt(
    lastDate: LocalDateTime,
    nextDate: LocalDateTime,
    groupId: GroupId,
  ): LocalDateTime {
    val minRefreshRateInMinutes =
      (getFeatureInt(FeatureName.refreshRateInMinutesLowerLimitInt, groupId) ?: 0).coerceAtLeast(1)
    val minNextDate = lastDate.plus(minRefreshRateInMinutes, ChronoUnit.MINUTES)

    return if (nextDate < minNextDate) {
      minNextDate
    } else {
      nextDate
    }
  }

  fun coerceRetentionMaxAgeDays(maxAge: Int?, groupId: GroupId): Int? {
    val minItems = getFeatureInt(FeatureName.repositoryRetentionMaxDaysLowerLimitInt, groupId)?.toInt()
    return minItems?.let { maxAge?.coerceAtLeast(minItems) }
  }

  fun auditCronExpression(cronString: String): String {
    CronExpression.isValidExpression(cronString)
    CronExpression.parse(cronString)
    return cronString
  }

  fun coerceVisibility(groupId: GroupId, visibility: EntityVisibility?): EntityVisibility {

    val canPublic = getFeatureBool(
      FeatureName.publicRepositoryBool,
      groupId
    ) ?: false
    return if (canPublic) {
      visibility ?: EntityVisibility.isPrivate
    } else {
      if (visibility !== EntityVisibility.isPublic) {
        log.info("overwrite visibility to $visibility")
      }
      EntityVisibility.isPrivate
    }
  }

  fun auditScrapeRequestMaxActions(actionsCount: Int?, groupId: GroupId) {
    actionsCount
      ?.let {
        val maxActions = getFeatureInt(FeatureName.scrapeRequestActionMaxCountInt, groupId)
        if (maxActions != null && maxActions < actionsCount) {
          throw IllegalArgumentException("Too many actions (limit $maxActions, actual $actionsCount")
        }
      }
  }

  fun auditScrapeRequestTimeout(timeout: Int?, groupId: GroupId) {
    timeout
      ?.let {
        val maxTimeout = getFeatureInt(FeatureName.scrapeRequestTimeoutMsecInt, groupId)
        if (maxTimeout != null && maxTimeout < it) {
          throw IllegalArgumentException("Timeout exceedes limit (limit $maxTimeout, actual $it")
        }
      }
  }

  fun auditRepositoryMaxCount(count: Int, groupId: GroupId) {
    val maxRepoCount = getFeatureInt(FeatureName.repositoriesMaxCountTotalInt, groupId)
    if (maxRepoCount != null && maxRepoCount < count) {
      throw IllegalArgumentException("Too many repository (limit $maxRepoCount, actual $count")
    }
  }

  fun violatesRepositoriesMaxActiveCount(groupId: GroupId): Boolean {
    val activeCount = repositoryRepository.countByGroupIdAndArchivedIsFalseAndSourcesSyncCronIsNot(groupId, "")
    return getFeatureInt(FeatureName.repositoriesMaxCountActiveInt, groupId)?.let { it <= activeCount } ?: false
  }

  fun auditSourcesMaxCountPerRepository(count: Int, groupId: GroupId) {
    val maxRequests = getFeatureInt(FeatureName.sourceMaxCountPerRepositoryInt, groupId)
    if (maxRequests != null && maxRequests < count) {

      throw IllegalArgumentException("Too many requests in source (limit $maxRequests, actual $count)")
    }
  }

  private fun getFeatureInt(featureName: FeatureName, groupId: GroupId): Long? =
    getFeature(featureName, groupId)?.valueInt

  private fun getFeatureBool(
    featureName: FeatureName,
    groupId: GroupId,
  ): Boolean? =
    getFeature(featureName, groupId)?.valueBoolean

  private fun getFeature(
    featureName: FeatureName,
    groupId: GroupId,
  ): FeatureValue? {
    return try {
      if (environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))) {
        featureValueRepository.resolveByFeatureGroupIdAndName(
          featureGroupRepository.findByParentFeatureGroupIdIsNull()!!.id,
          featureName.name
        )
      } else {
        val featureGroup = featureGroupRepository.findByGroupId(groupId)!!
//        if (plan == null) {
//          runBlocking {
//            productService.enableDefaultSaasProduct(product, planId)
//          }
//          plan = planRepository.findActiveByUserAndProductIn(
//            planId,
//            listOf(product, Vertical.feedless),
//            LocalDateTime.now()
//          )!!
//        }

        featureValueRepository.resolveByFeatureGroupIdAndName(featureGroup.id, featureName.name)
      }
    } catch (e: Exception) {
      log.error("getFeature featureName=$featureName planId=$groupId failed: ${e.message}")
      null
    }
  }
}

