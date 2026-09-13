package org.migor.feedless.http.mapper

import org.migor.feedless.plan.Plan
import org.migor.feedless.util.toOffsetDateTime
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.Plan as HttpPlan

@Component
class HttpPlanMapper {

  fun toHttp(plan: Plan): HttpPlan =
    HttpPlan(
      id = plan.id.uuid,
      productId = plan.productId.uuid,
      startedAt = plan.startedAt?.toOffsetDateTime(),
      terminatedAt = plan.terminatedAt?.toOffsetDateTime(),
      recurringYearly = false,
    )
}
