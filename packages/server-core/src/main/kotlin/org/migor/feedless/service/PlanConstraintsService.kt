package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class PlanConstraintsService {

  @Autowired
  lateinit var currentUser: CurrentUser

  fun patchRetentionMaxItems(maxItems: Int?) = maxItems
    ?.coerceAtLeast(2)
    ?.coerceAtMost(10)

  fun patchRetentionMaxAgeDays(maxAge: Int?) = maxAge
    ?.coerceAtLeast(2)
    ?.coerceAtMost(256)

  fun auditRefreshCron(cronString: String): String {
    CronExpression.isValidExpression(cronString)
    CronExpression.parse(cronString)
    return cronString
  }

  fun patchVisibility(visibility: EntityVisibility) = visibility

  fun auditScrapeRequestMaxActions(actionsCount: Int?) = actionsCount
    ?.coerceAtMost(10)

  fun auditScrapeRequestTimeout(timeout: Int?) = timeout
    ?.coerceAtMost(30000)

}
