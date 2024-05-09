package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.repository.toDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.generated.types.PlanSubscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Profile(AppProfiles.database)
class PlanSubscriptionDataResolver {

  @Autowired
  lateinit var planService: PlanService

  @DgsData(parentType = DgsConstants.PLANSUBSCRIPTION.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun plan(dfe: DgsDataFetchingEnvironment): Plan = coroutineScope {
    val subscription: PlanSubscription = dfe.getSource()
    planService.findById(subscription.planId)
      .orElseThrow { NotFoundException("plan not found") }.toDto()
  }
}
