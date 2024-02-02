package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.repositories.FeatureDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.util.toDate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile(AppProfiles.database)
class PlanConstraintsService {

  private val log = LoggerFactory.getLogger(PlanConstraintsService::class.simpleName)


  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var featureDAO: FeatureDAO

  @Autowired
  lateinit var userDAO: UserDAO

  fun coerceRetentionMaxItems(maxItems: Int?, userId: UUID): Int {
    return (maxItems ?: 0)
      .coerceAtLeast(4)
      .coerceAtMost(getFeatureInt(FeatureName.scrapeSourceRetentionMaxItemsInt, userId))
  }

  fun coerceMinScheduledNextAt(nextDate: Date, userId: UUID): Date {
    val minRefreshRateInMinutes = getFeatureInt(FeatureName.minRefreshRateInMinutesInt, userId)
    val minNextDate = toDate(LocalDateTime.now().plus(minRefreshRateInMinutes.toLong(), ChronoUnit.MINUTES))

    return if (nextDate < minNextDate) {
      nextDate
    } else {
      minNextDate
    }
  }

  fun coerceRetentionMaxAgeDays(maxAge: Int?): Int = (maxAge ?: 0)
    .coerceAtLeast(2)

  fun auditRefreshCron(cronString: String): String {
    CronExpression.isValidExpression(cronString)
    CronExpression.parse(cronString)
    return cronString
  }

  fun coerceVisibility(visibility: EntityVisibility): EntityVisibility {
    return if (visibility === EntityVisibility.isPublic && getFeatureBool(FeatureName.publicScrapeSourceBool, userIdFromRequest())) {
      visibility
    } else {
      EntityVisibility.isPrivate
    }
  }

  fun auditScrapeRequestMaxActions(actionsCount: Int?, userId: UUID) {
    actionsCount
      ?.let {
        val maxActions = getFeatureInt(FeatureName.scrapeRequestActionMaxCountInt, userId)
        if (maxActions < actionsCount) {
          throw IllegalArgumentException("Too many actions (limit $maxActions, actual $actionsCount")
        }
      }
  }

//  fun coerceScrapeRequestTimeout(timeout: Int?): Int? {
//    return timeout
//      ?.coerceAtLeast(10000)
//      ?.coerceAtMost(getFeatureInt(FeatureName.scrapeRequestTimeout, userIdFromRequest()))
//  }

  fun auditScrapeRequestTimeout(timeout: Int?, userId: UUID) {
    timeout
      ?.let {
        val maxTimeout = getFeatureInt(FeatureName.scrapeRequestTimeoutInt, userId)
        if (maxTimeout < it) {
          throw IllegalArgumentException("Timeout exceedes limit (limit $maxTimeout, actual $it")
        }
      }
  }

  fun coerceScrapeSourceExpiry(corrId: String, userId: UUID): Date? {
    val user = userDAO.findById(userId).orElseThrow()
    return if (user.anonymous) {
      val days = featureDAO.findFirstByName(FeatureName.scrapeSourceExpiryInDaysInt).valueInt!!
      val expiry = LocalDateTime.now().plus(days.toLong(), ChronoUnit.DAYS)
      log.info("[$corrId] set expiry to $expiry")
      toDate(expiry)
    } else {
      null
    }
  }

  private fun userIdFromRequest() = currentUser.userId()!!

  private fun getFeatureInt(featureName: FeatureName, userId: UUID) = getFeature(featureName, userId)!!.valueInt!!

  private fun getFeatureBool(featureName: FeatureName, userId: UUID) = getFeature(featureName, userId)!!.valueBoolean!!

  private fun getFeature(featureName: FeatureName, userId: UUID): FeatureEntity? {
    val user = userDAO.findById(userId).orElseThrow()
    return featureDAO.findByPlanIdAndName(user.planId, featureName)
  }

  fun auditScrapeSourceMaxCount(count: Int, userId: UUID) {
    val maxSubscriptions = getFeatureInt(FeatureName.scrapeSourceMaxCountTotalInt, userId)
    if (maxSubscriptions < count) {
      throw IllegalArgumentException("Too many subscriptions (limit $maxSubscriptions, actual $count")
    }
  }

  fun violatesScrapeSourceMaxActiveCount(activeCount: Int, userId: UUID): Boolean {
    return getFeatureInt(FeatureName.scrapeSourceMaxCountActiveInt, userId) <= activeCount
  }

  fun auditScrapeRequestMaxCountPerSource(count: Int, userId: UUID) {
    val maxRequests = getFeatureInt(FeatureName.scrapeRequestMaxCountPerSourceInt, userId)
    if (maxRequests < count) {
      throw IllegalArgumentException("Too many requests in source (limit $maxRequests, actual $count)")
    }
  }
}
