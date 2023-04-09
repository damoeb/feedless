package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.PlanSubscription
import org.migor.rich.rss.generated.types.User
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Profile(AppProfiles.database)
class UserDataResolver {

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun subscription(dfe: DgsDataFetchingEnvironment): PlanSubscription? = coroutineScope {
    val user: User = dfe.getSource()
    if (user.subscriptionId == null) {
      null
    } else {
      toDTO(user.subscription)
    }
  }

}
