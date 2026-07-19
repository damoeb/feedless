package org.migor.feedless.http.mapper

import org.migor.feedless.plan.Plan
import org.migor.feedless.util.toMillis
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.Plan as HttpPlan

@Component
class HttpPlanMapper {

  fun toHttp(plan: Plan): HttpPlan =
    HttpPlan(
      id = plan.id.uuid,
      productId = plan.productId.uuid,
      startedAt = plan.startedAt?.toMillis(),
      terminatedAt = plan.terminatedAt?.toMillis(),
      recurringYearly = false,
    )
}
