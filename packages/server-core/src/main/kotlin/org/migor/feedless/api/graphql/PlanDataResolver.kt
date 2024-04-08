package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import org.migor.feedless.AppProfiles
import org.migor.feedless.service.FeatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class PlanDataResolver {

  @Autowired
  lateinit var featureService: FeatureService

//  @DgsData(parentType = DgsConstants.PLAN.TYPE_NAME)
//  @Transactional(propagation = Propagation.REQUIRED)
//  suspend fun features(dfe: DgsDataFetchingEnvironment): List<Feature> = coroutineScope {
//    val plan: Plan = dfe.getSource()
//    featureService.findAllByPlanId(UUID.fromString(plan.id)).map { it.toDto() }
//  }

}
