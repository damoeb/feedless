package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureValueEntity
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.repositories.FeatureValueDAO
import org.migor.feedless.data.jpa.repositories.PlanDAO
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserDAO
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
@Profile("${AppProfiles.database} & !${AppProfiles.selfHosted}")
class PlanConstraintsServiceImpl : PlanConstraintsService {

  private val log = LoggerFactory.getLogger(PlanConstraintsServiceImpl::class.simpleName)

  @Autowired
  lateinit var sessionService: SessionService

  @Autowired
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Autowired
  lateinit var featureDAO: FeatureValueDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var planDAO: PlanDAO

  override fun coerceRetentionMaxItems(customMaxItems: Int?, userId: UUID): Int? {
    val maxItems = getFeatureInt(FeatureName.scrapeSourceRetentionMaxItemsInt, userId)
    return customMaxItems?.let {
      maxItems?.let {
        customMaxItems.coerceAtLeast(2)
          .coerceAtMost(maxItems)
      } ?: customMaxItems.coerceAtLeast(2)
    }
  }

  override fun coerceMinScheduledNextAt(nextDate: Date, userId: UUID): Date {
    val minRefreshRateInMinutes = getFeatureInt(FeatureName.minRefreshRateInMinutesInt, userId)!!
    val minNextDate = toDate(LocalDateTime.now().plus(minRefreshRateInMinutes.toLong(), ChronoUnit.MINUTES))

    return if (nextDate < minNextDate) {
      minNextDate
    } else {
      nextDate
    }
  }

  override fun coerceRetentionMaxAgeDays(maxAge: Int?): Int? = maxAge?.coerceAtLeast(2)

  override fun auditRefreshCron(cronString: String): String {
    CronExpression.isValidExpression(cronString)
    CronExpression.parse(cronString)
    return cronString
  }

  override fun coerceVisibility(visibility: EntityVisibility?): EntityVisibility {
    val canPublic = getFeatureBool(
      FeatureName.publicScrapeSourceBool,
      userIdFromRequest()
    ) ?: false
    return if (canPublic) {
      visibility ?: EntityVisibility.isPrivate
    } else {
      EntityVisibility.isPrivate
    }
  }

  override fun auditScrapeRequestMaxActions(actionsCount: Int?, userId: UUID) {
    actionsCount
      ?.let {
        val maxActions = getFeatureInt(FeatureName.scrapeRequestActionMaxCountInt, userId)
        if (maxActions != null && maxActions < actionsCount) {
          throw IllegalArgumentException("Too many actions (limit $maxActions, actual $actionsCount")
        }
      }
  }

  override fun auditScrapeRequestTimeout(timeout: Int?, userId: UUID) {
    timeout
      ?.let {
        val maxTimeout = getFeatureInt(FeatureName.scrapeRequestTimeoutInt, userId)
        if (maxTimeout != null && maxTimeout < it) {
          throw IllegalArgumentException("Timeout exceedes limit (limit $maxTimeout, actual $it")
        }
      }
  }

  override fun coerceScrapeSourceExpiry(corrId: String, userId: UUID): Date? {
    val user = userDAO.findById(userId).orElseThrow()
    return if (user.anonymous) {
      val days = getFeatureInt(FeatureName.scrapeSourceExpiryInDaysInt, userId)!!
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
    return user.planId?.let {
      featureDAO.findByPlanIdAndName(it.toString(), featureName.name).firstOrNull()
    } ?: featureDAO.findByPlanIdAndName(planDAO.findFirstByName(PlanName.system.name)!!.id.toString(), featureName.name)
      .firstOrNull()
  }

  override fun auditScrapeSourceMaxCount(count: Int, userId: UUID) {
    val maxSubscriptions = getFeatureInt(FeatureName.scrapeSourceMaxCountTotalInt, userId)
    if (maxSubscriptions != null && maxSubscriptions < count) {
      throw IllegalArgumentException("Too many subscriptions (limit $maxSubscriptions, actual $count")
    }
  }

  override fun violatesScrapeSourceMaxActiveCount(userId: UUID): Boolean {
    val activeCount = sourceSubscriptionDAO.countByOwnerIdAndArchived(userId, false)
    return getFeatureInt(FeatureName.scrapeSourceMaxCountActiveInt, userId)!! <= activeCount
  }

  override fun auditScrapeRequestMaxCountPerSource(count: Int, userId: UUID) {
    val maxRequests = getFeatureInt(FeatureName.scrapeRequestMaxCountPerSourceInt, userId)
    if (maxRequests != null && maxRequests < count) {
      throw IllegalArgumentException("Too many requests in source (limit $maxRequests, actual $count)")
    }
  }
}
