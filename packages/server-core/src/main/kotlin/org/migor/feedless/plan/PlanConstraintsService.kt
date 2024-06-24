package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.session.SessionService
import org.migor.feedless.subscription.PlanDAO
import org.migor.feedless.user.UserDAO
import org.migor.feedless.util.toDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*


@Service
@Profile(AppProfiles.database)
class PlanConstraintsService {

  @Autowired
  private lateinit var planDAO: PlanDAO
  private val log = LoggerFactory.getLogger(PlanConstraintsService::class.simpleName)

  @Autowired
  private lateinit var environment: Environment

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var featureValueDAO: FeatureValueDAO

  @Autowired
  private lateinit var featureGroupDAO: FeatureGroupDAO

  fun coerceRetentionMaxItems(customMaxItems: Int?, userId: UUID, product: ProductCategory): Int? {
    val minItems = (getFeatureInt(FeatureName.repositoryCapacityLowerLimitInt, userId, product) ?: 0).coerceAtLeast(2).toInt()
    val maxItems = getFeatureInt(FeatureName.repositoryCapacityUpperLimitInt, userId, product)?.toInt()
    return customMaxItems?.let {
      maxItems?.let {
        customMaxItems.coerceAtLeast(minItems)
          .coerceAtMost(maxItems)
      } ?: customMaxItems.coerceAtLeast(minItems)
    } ?: maxItems
  }

  private fun resolveProduct(product: ProductCategory?): ProductCategory {
    return product ?: sessionService.activeProductFromRequest()!!
  }

  fun coerceMinScheduledNextAt(lastDate: Date, nextDate: Date, userId: UUID, product: ProductCategory): Date {
    val minRefreshRateInMinutes = (getFeatureInt(FeatureName.refreshRateInMinutesLowerLimitInt, userId, product) ?: 0).coerceAtLeast(1)
    val minNextDate = toDate(
      lastDate.toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime().plus(minRefreshRateInMinutes, ChronoUnit.MINUTES)
    )

    return if (nextDate < minNextDate) {
      minNextDate
    } else {
      nextDate
    }
  }

  fun coerceRetentionMaxAgeDays(maxAge: Int?, ownerId: UUID, product: ProductCategory): Int? {
    val minItems = getFeatureInt(FeatureName.repositoryRetentionMaxDaysLowerLimitInt, ownerId, product)?.toInt()
    return minItems?.let { maxAge?.coerceAtLeast(minItems) }
  }

  fun auditCronExpression(cronString: String): String {
    CronExpression.isValidExpression(cronString)
    CronExpression.parse(cronString)
    return cronString
  }

  fun coerceVisibility(corrId: String, visibility: EntityVisibility?): EntityVisibility {
    val canPublic = getFeatureBool(
      FeatureName.publicRepositoryBool,
      userIdFromRequest()
    ) ?: false
    return if (canPublic) {
      visibility ?: EntityVisibility.isPrivate
    } else {
      if (visibility !== EntityVisibility.isPublic) {
        log.info("[$corrId] overwrite visibility to $visibility")
      }
      EntityVisibility.isPrivate
    }
  }

  fun auditScrapeRequestMaxActions(actionsCount: Int?, userId: UUID) {
    actionsCount
      ?.let {
        val maxActions = getFeatureInt(FeatureName.scrapeRequestActionMaxCountInt, userId)
        if (maxActions != null && maxActions < actionsCount) {
          throw IllegalArgumentException("Too many actions (limit $maxActions, actual $actionsCount")
        }
      }
  }

  fun auditScrapeRequestTimeout(timeout: Int?, userId: UUID) {
    timeout
      ?.let {
        val maxTimeout = getFeatureInt(FeatureName.scrapeRequestTimeoutMsecInt, userId)
        if (maxTimeout != null && maxTimeout < it) {
          throw IllegalArgumentException("Timeout exceedes limit (limit $maxTimeout, actual $it")
        }
      }
  }

  private fun userIdFromRequest() = sessionService.userId()!!

  private fun getFeatureInt(featureName: FeatureName, userId: UUID, product: ProductCategory? = null): Long? = getFeature(featureName, userId, resolveProduct(product))?.valueInt

  private fun getFeatureBool(featureName: FeatureName, userId: UUID, product: ProductCategory? = null): Boolean? =
    getFeature(featureName, userId, resolveProduct(product))?.valueBoolean

  private fun getFeature(featureName: FeatureName, userId: UUID, product: ProductCategory): FeatureValueEntity? {
    val user = userDAO.findById(userId).orElseThrow()
    return try {
      if (environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))) {
        featureValueDAO.resolveByFeatureGroupIdAndName(featureGroupDAO.findByParentFeatureGroupIdIsNull()!!.id, featureName.name)
      } else {
        planDAO.findActiveByUserAndProduct(userId, product)?.let {
          featureValueDAO.resolveByFeatureGroupIdAndName(it.product!!.featureGroupId!!, featureName.name)
        } ?: throw IllegalArgumentException("user has no subscription")
      }
    } catch (e: Exception) {
      log.error("[corrId] ${e.message}")
      null
    }
  }

  fun auditScrapeSourceMaxCount(count: Int, userId: UUID) {
    val maxSubscriptions = getFeatureInt(FeatureName.scrapeSourceMaxCountTotalInt, userId)
    if (maxSubscriptions != null && maxSubscriptions < count) {
      throw IllegalArgumentException("Too many subscriptions (limit $maxSubscriptions, actual $count")
    }
  }

  fun violatesScrapeSourceMaxActiveCount(userId: UUID): Boolean {
    val activeCount = repositoryDAO.countByOwnerIdAndArchived(userId, false)
    return getFeatureInt(FeatureName.scrapeSourceMaxCountActiveInt, userId)!! <= activeCount
  }

  fun auditScrapeRequestMaxCountPerSource(count: Int, userId: UUID, product: ProductCategory) {
    val maxRequests = getFeatureInt(FeatureName.scrapeRequestMaxCountPerSourceInt, userId, product)
    if (maxRequests != null && maxRequests < count) {
      throw IllegalArgumentException("Too many requests in source (limit $maxRequests, actual $count)")
    }
  }
}
