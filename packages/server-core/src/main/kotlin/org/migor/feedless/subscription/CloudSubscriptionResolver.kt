package org.migor.feedless.subscription

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CloudSubscription
import org.migor.feedless.generated.types.CreateUserInput
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.plan.fromDto
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api}")
class CloudSubscriptionResolver {

  private val log = LoggerFactory.getLogger(CloudSubscriptionResolver::class.simpleName)

  @Autowired
  private lateinit var cloudSubscriptionDAO: CloudSubscriptionDAO


  @DgsData(parentType = DgsConstants.USER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun subscription(dfe: DgsDataFetchingEnvironment): CloudSubscription? = coroutineScope {
    val user: User = dfe.getSource()
    user.subscriptionId?.let {
      cloudSubscriptionDAO.findById(UUID.fromString(user.subscriptionId)).orElseThrow().toDto()
    }
  }
}

private fun CloudSubscriptionEntity.toDto(): CloudSubscription {
  return CloudSubscription.newBuilder()
    .productId(productId.toString())
    .startedAt(startedAt?.time)
    .terminatedAt(terminatedAt?.time)
    .build()
}

