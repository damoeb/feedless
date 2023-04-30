package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.Plan
import org.migor.rich.rss.generated.types.PlanSubscription
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.PlanService
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
    toDTO(planService.findById(subscription.planId)
      .orElseThrow { IllegalArgumentException("plan not found") })
  }

}
