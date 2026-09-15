package org.migor.feedless.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.guard.ResourceGuard
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
class PlanGuard(private val planRepository: PlanRepository) : ResourceGuard<PlanId, Plan> {

  override suspend fun requireRead(id: PlanId): Plan {
    TODO("Not yet implemented")
  }

  override suspend fun requireWrite(id: PlanId): Plan {
    TODO("Not yet implemented")
  }

  override suspend fun requireExecute(id: PlanId): Plan {
    TODO("Not yet implemented")
  }
}
