package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.auth.CurrentUser
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.User
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.migor.rich.rss.generated.types.Profile as ProfileDto

@DgsComponent
@Profile(AppProfiles.database)
class ProfileDataResolver {

  @Autowired
  lateinit var currentUser: CurrentUser

  @DgsData(parentType = DgsConstants.PROFILE.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun user(dfe: DgsDataFetchingEnvironment): User? = coroutineScope {
    val profile: ProfileDto = dfe.getSource()
    profile.userId?.let {toDTO(currentUser.user()) }
  }

}
