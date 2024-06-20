package org.migor.feedless.subscription

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.CloudSubscription
import org.migor.feedless.generated.types.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api}")
class CloudSubscriptionResolver {

  private val log = LoggerFactory.getLogger(CloudSubscriptionResolver::class.simpleName)

  @Autowired
  private lateinit var cloudSubscriptionDAO: CloudSubscriptionDAO


  @DgsData(parentType = DgsConstants.USER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
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

