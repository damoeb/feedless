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
      .coerceAtMost(getFeatureInt(FeatureName.itemsRetention, userId))
  }

  fun coerceMinScheduledNextAt(nextDate: Date, userId: UUID): Date {
    val minRefreshRateInMinutes = getFeatureInt(FeatureName.minRefreshRateInMinutes, userId)
    val minNextDate = toDate(LocalDateTime.now().plus(minRefreshRateInMinutes.toLong(), ChronoUnit.MINUTES))

    return if (nextDate < minNextDate) {
      nextDate
    } else {
      minNextDate
    }
  }

  fun coerceRetentionMaxAgeDays(maxAge: Int?) = maxAge
    ?.coerceAtLeast(2)

  fun auditRefreshCron(cronString: String): String {
    CronExpression.isValidExpression(cronString)
    CronExpression.parse(cronString)
    return cronString
  }

  fun coerceVisibility(visibility: EntityVisibility): EntityVisibility {
    return if (visibility === EntityVisibility.isPublic && getFeatureBool(FeatureName.publicScrapeSource, userIdFromRequest())) {
      visibility
    } else {
      EntityVisibility.isPrivate
    }
  }

  fun coerceScrapeRequestMaxActions(actionsCount: Int?) = actionsCount
    ?.coerceAtMost(10)

  fun coerceScrapeRequestTimeout(timeout: Int?): Int? {
    return timeout
      ?.coerceAtLeast(10000)
      ?.coerceAtMost(getFeatureInt(FeatureName.scrapeRequestTimeout, userIdFromRequest()))
  }

  fun coerceScrapeSourceExpiry(corrId: String, userId: UUID): Date? {
    val user = userDAO.findById(userId).orElseThrow()
    return if (user.anonymous) {
      val days = featureDAO.findFirstByName(FeatureName.scrapeSourceExpiryInDays).valueInt!!
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

//  fun can(feature: FeatureName): Boolean {
//    TODO("Not yet implemented")
//  }
}
