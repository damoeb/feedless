package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.util.toDate
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.selfHosted}")
class PlanConstraintsSelfHostedService : PlanConstraintsService {

  private val log = LoggerFactory.getLogger(PlanConstraintsSelfHostedService::class.simpleName)
  override fun coerceRetentionMaxItems(maxItems: Int?, userId: UUID): Int? {
    return maxItems?.coerceAtLeast(2)
  }

  override fun coerceMinScheduledNextAt(nextDate: Date, userId: UUID): Date {
    val minNextDate = toDate(LocalDateTime.now().plus(5, ChronoUnit.MINUTES))

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
    return if (visibility === EntityVisibility.isPublic) {
      visibility
    } else {
      EntityVisibility.isPrivate
    }
  }

  override fun auditScrapeRequestMaxActions(actionsCount: Int?, userId: UUID) {
  }

  override fun auditScrapeRequestTimeout(timeout: Int?, userId: UUID) {
  }

  override fun coerceScrapeSourceExpiry(corrId: String, userId: UUID): Date? {
    return null
  }

  override fun auditScrapeSourceMaxCount(count: Int, userId: UUID) {
  }

  override fun violatesScrapeSourceMaxActiveCount(activeCount: Int, userId: UUID): Boolean {
    return false
  }

  override fun auditScrapeRequestMaxCountPerSource(count: Int, userId: UUID) {
  }
}
