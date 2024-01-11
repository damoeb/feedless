package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class PlanConstraintsService {

  fun patchRetentionMaxItems(maxItems: Int?) = (maxItems ?: 10)
    .coerceAtLeast(2)
    .coerceAtMost(10)

  fun patchRetentionMaxAgeDays(maxAge: Int?) = (maxAge ?: 10)
    .coerceAtLeast(2)
    .coerceAtMost(256)

  fun auditRefreshCron(cronString: String) = cronString

  fun patchVisibility(visibility: EntityVisibility) = visibility

  fun auditScrapeRequestMaxActions(actionsCount: Int?) = actionsCount

  fun auditScrapeRequestTimeout(timeout: Int?) = timeout

}
