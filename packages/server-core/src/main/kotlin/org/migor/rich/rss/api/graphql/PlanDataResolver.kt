package org.migor.rich.rss.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.Feature
import org.migor.rich.rss.generated.types.Plan
import org.migor.rich.rss.api.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.FeatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class PlanDataResolver {

  @Autowired
  lateinit var featureService: FeatureService

  @DgsData(parentType = DgsConstants.PLAN.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun features(dfe: DgsDataFetchingEnvironment): List<Feature> = coroutineScope {
    val plan: Plan = dfe.getSource()
    featureService.findAllByPlanId(UUID.fromString(plan.id)).map { toDTO(it) }
  }

}
