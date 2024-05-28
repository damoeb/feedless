package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserDAO
import org.migor.feedless.util.toDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile(AppProfiles.database)
class PlanConstraintsService {

  private val log = LoggerFactory.getLogger(PlanConstraintsService::class.simpleName)

  @Autowired
  lateinit var sessionService: SessionService

  @Autowired
  lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  lateinit var featureDAO: FeatureValueDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var planDAO: PlanDAO

  fun coerceRetentionMaxItems(customMaxItems: Int?, userId: UUID): Int? {
    val minItems = getFeatureInt(FeatureName.repositoryRetentionMaxItemsLowerLimitInt, userId)!!
    val maxItems = getFeatureInt(FeatureName.repositoryRetentionMaxItemsUpperLimitInt, userId)
    return customMaxItems?.let {
      maxItems?.let {
        customMaxItems.coerceAtLeast(minItems)
          .coerceAtMost(maxItems)
      } ?: customMaxItems.coerceAtLeast(minItems)
    } ?: maxItems
  }

  fun coerceMinScheduledNextAt(lastDate: Date, nextDate: Date, userId: UUID): Date {
    val minRefreshRateInMinutes = getFeatureInt(FeatureName.refreshRateInMinutesLowerLimitInt, userId)!!
    val minNextDate = toDate(
      lastDate.toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime().plus(minRefreshRateInMinutes.toLong(), ChronoUnit.MINUTES)
    )

    return if (nextDate < minNextDate) {
      minNextDate
    } else {
      nextDate
    }
  }

  fun coerceRetentionMaxAgeDays(maxAge: Int?, ownerId: UUID): Int? {
    val minItems = getFeatureInt(FeatureName.repositoryRetentionMaxDaysLowerLimitInt, ownerId)
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
        val maxTimeout = getFeatureInt(FeatureName.scrapeRequestTimeoutInt, userId)
        if (maxTimeout != null && maxTimeout < it) {
          throw IllegalArgumentException("Timeout exceedes limit (limit $maxTimeout, actual $it")
        }
      }
  }

  fun coerceScrapeSourceExpiry(corrId: String, userId: UUID): Date? {
    val user = userDAO.findById(userId).orElseThrow()
    return if (user.anonymous) {
      val days = getFeatureInt(FeatureName.repositoryWhenAnonymousExpiryInDaysInt, userId)!!
      val expiry = LocalDateTime.now().plus(days.toLong(), ChronoUnit.DAYS)
      log.info("[$corrId] assign expiry to $expiry")
      toDate(expiry)
    } else {
      null
    }
  }

  private fun userIdFromRequest() = sessionService.userId()!!

  private fun getFeatureInt(featureName: FeatureName, userId: UUID): Int? = getFeature(featureName, userId)?.valueInt

  private fun getFeatureBool(featureName: FeatureName, userId: UUID): Boolean? =
    getFeature(featureName, userId)?.valueBoolean

  private fun getFeature(featureName: FeatureName, userId: UUID): FeatureValueEntity? {
    val user = userDAO.findById(userId).orElseThrow()
    return try {
      user.planId?.let {
        featureDAO.findByPlanIdAndName(it, featureName.name).firstOrNull()
      } ?: featureDAO.findByPlanIdAndName(planDAO.findFirstByName(PlanName.system.name)!!.id, featureName.name)
        .firstOrNull()
    } catch (e: Exception) {
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

  fun auditScrapeRequestMaxCountPerSource(count: Int, userId: UUID) {
    val maxRequests = getFeatureInt(FeatureName.scrapeRequestMaxCountPerSourceInt, userId)
    if (maxRequests != null && maxRequests < count) {
      throw IllegalArgumentException("Too many requests in source (limit $maxRequests, actual $count)")
    }
  }
}
