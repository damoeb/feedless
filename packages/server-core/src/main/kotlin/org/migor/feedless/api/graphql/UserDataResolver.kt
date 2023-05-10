package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.data.jpa.repositories.UserSecretDAO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.PlanSubscription
import org.migor.feedless.generated.types.User
import org.migor.feedless.generated.types.UserSecret
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class UserDataResolver {

  @Autowired
  lateinit var userSecretDAO: UserSecretDAO

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

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun secrets(dfe: DgsDataFetchingEnvironment): List<UserSecret> = coroutineScope {
    val user: User = dfe.getSource()
    userSecretDAO.findByOwnerId(UUID.fromString(user.id)).map { DtoResolver.toDTO(it) }
  }

}
