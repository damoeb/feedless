package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureValueDAO
import org.migor.feedless.feature.FeatureValueEntity
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.corrId
import org.migor.feedless.user.userId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.coroutines.coroutineContext


@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
@Transactional(propagation = Propagation.NEVER)
class PlanConstraintsService {

  private val log = LoggerFactory.getLogger(PlanConstraintsService::class.simpleName)

  @Autowired
  private lateinit var planDAO: PlanDAO

  @Autowired
  private lateinit var environment: Environment

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var featureValueDAO: FeatureValueDAO

  @Autowired
  private lateinit var productService: ProductService

  @Autowired
  private lateinit var featureGroupDAO: FeatureGroupDAO

  @Transactional(readOnly = true)
  suspend fun coerceRetentionMaxCapacity(customMaxItems: Int?, userId: UUID, product: Vertical): Int? {
    val minItems =
      (getFeatureInt(FeatureName.repositoryCapacityLowerLimitInt, userId, product) ?: 0).coerceAtLeast(2).toInt()
    val maxItems = getFeatureInt(FeatureName.repositoryCapacityUpperLimitInt, userId, product)?.toInt()
    return customMaxItems?.let {
      maxItems?.let {
        customMaxItems.coerceAtLeast(minItems)
          .coerceAtMost(maxItems)
      } ?: customMaxItems.coerceAtLeast(minItems)
    } ?: maxItems
  }

  @Transactional(readOnly = true)
  suspend fun coerceMinScheduledNextAt(
    lastDate: LocalDateTime,
    nextDate: LocalDateTime,
    userId: UUID,
    product: Vertical
  ): LocalDateTime {
    val minRefreshRateInMinutes =
      (getFeatureInt(FeatureName.refreshRateInMinutesLowerLimitInt, userId, product) ?: 0).coerceAtLeast(1)
    val minNextDate = lastDate.plus(minRefreshRateInMinutes, ChronoUnit.MINUTES)

    return if (nextDate < minNextDate) {
      minNextDate
    } else {
      nextDate
    }
  }

  @Transactional(readOnly = true)
  suspend fun coerceRetentionMaxAgeDays(maxAge: Int?, ownerId: UUID, product: Vertical): Int? {
    val minItems = getFeatureInt(FeatureName.repositoryRetentionMaxDaysLowerLimitInt, ownerId, product)?.toInt()
    return minItems?.let { maxAge?.coerceAtLeast(minItems) }
  }

  fun auditCronExpression(cronString: String): String {
    CronExpression.isValidExpression(cronString)
    CronExpression.parse(cronString)
    return cronString
  }

  @Transactional(readOnly = true)
  suspend fun coerceVisibility(visibility: EntityVisibility?): EntityVisibility {
    val canPublic = getFeatureBool(
      FeatureName.publicRepositoryBool,
      coroutineContext.userId()
    ) ?: false
    return if (canPublic) {
      visibility ?: EntityVisibility.isPrivate
    } else {
      if (visibility !== EntityVisibility.isPublic) {
        log.info("[${coroutineContext.corrId()}] overwrite visibility to $visibility")
      }
      EntityVisibility.isPrivate
    }
  }

  @Transactional(readOnly = true)
  suspend fun auditScrapeRequestMaxActions(actionsCount: Int?, userId: UUID) {
    actionsCount
      ?.let {
        val maxActions = getFeatureInt(FeatureName.scrapeRequestActionMaxCountInt, userId)
        if (maxActions != null && maxActions < actionsCount) {
          throw IllegalArgumentException("Too many actions (limit $maxActions, actual $actionsCount")
        }
      }
  }

  @Transactional(readOnly = true)
  suspend fun auditScrapeRequestTimeout(timeout: Int?, userId: UUID) {
    timeout
      ?.let {
        val maxTimeout = getFeatureInt(FeatureName.scrapeRequestTimeoutMsecInt, userId)
        if (maxTimeout != null && maxTimeout < it) {
          throw IllegalArgumentException("Timeout exceedes limit (limit $maxTimeout, actual $it")
        }
      }
  }

  @Transactional(readOnly = true)
  suspend fun auditRepositoryMaxCount(count: Int, userId: UUID) {
    val maxRepoCount = getFeatureInt(FeatureName.repositoriesMaxCountTotalInt, userId)
    if (maxRepoCount != null && maxRepoCount < count) {
      throw IllegalArgumentException("Too many repository (limit $maxRepoCount, actual $count")
    }
  }

  @Transactional(readOnly = true)
  suspend fun violatesRepositoriesMaxActiveCount(userId: UUID): Boolean {
    val activeCount = withContext(Dispatchers.IO) {
      repositoryDAO.countByOwnerIdAndArchivedIsFalseAndSourcesSyncCronIsNot(userId, "")
    }
    return getFeatureInt(FeatureName.repositoriesMaxCountActiveInt, userId)?.let { it <= activeCount } ?: false
  }

  @Transactional(readOnly = true)
  suspend fun auditSourcesMaxCountPerRepository(count: Int, userId: UUID, product: Vertical) {
    val maxRequests = getFeatureInt(FeatureName.sourceMaxCountPerRepositoryInt, userId, product)
    if (maxRequests != null && maxRequests < count) {
      throw IllegalArgumentException("Too many requests in source (limit $maxRequests, actual $count)")
    }
  }

  private suspend fun getFeatureInt(featureName: FeatureName, userId: UUID, product: Vertical? = null): Long? =
    getFeature(featureName, userId, resolveProduct(product))?.valueInt

  private suspend fun getFeatureBool(
    featureName: FeatureName,
    userId: UUID,
    product: Vertical? = null
  ): Boolean? =
    getFeature(featureName, userId, resolveProduct(product))?.valueBoolean

  private suspend fun getFeature(
    featureName: FeatureName,
    userId: UUID,
    product: Vertical
  ): FeatureValueEntity? {
    return try {
      withContext(Dispatchers.IO) {
        if (environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))) {
          featureValueDAO.resolveByFeatureGroupIdAndName(
            featureGroupDAO.findByParentFeatureGroupIdIsNull()!!.id,
            featureName.name
          )
        } else {
          var plan =
            planDAO.findActiveByUserAndProductIn(userId, listOf(product, Vertical.feedless), LocalDateTime.now())
          if (plan == null) {
            productService.enableDefaultSaasProduct(product, userId)
            plan = planDAO.findActiveByUserAndProductIn(userId, listOf(product, Vertical.feedless), LocalDateTime.now())
          }

          featureValueDAO.resolveByFeatureGroupIdAndName(plan!!.product!!.featureGroupId, featureName.name)
        }
      }
    } catch (e: Exception) {
      log.error("getFeature featureName=$featureName userId=$userId product=$product failed: ${e.message}")
      null
    }
  }

  private suspend fun resolveProduct(product: Vertical?): Vertical {
    return product ?: currentCoroutineContext()[RequestContext]?.product!!
  }
}
