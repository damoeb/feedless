package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.repositories.UserSecretDAO
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.PlanSubscription
import org.migor.rich.rss.generated.types.User
import org.migor.rich.rss.generated.types.UserSecret
import org.migor.rich.rss.graphql.DtoResolver.toDTO
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
