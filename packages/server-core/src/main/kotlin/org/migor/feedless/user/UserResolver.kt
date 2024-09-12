package org.migor.feedless.user

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.Session
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.useRequestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.user} & ${AppLayer.api}")
@Transactional
class UserResolver {

  private val log = LoggerFactory.getLogger(UserResolver::class.simpleName)

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var sessionService: SessionService

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateCurrentUser)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun updateCurrentUser(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpdateCurrentUserInput,
  ): Boolean = withContext(useRequestContext(currentCoroutineContext())) {
    log.info("[$corrId] updateCurrentUser ${sessionService.userId()} $data")
    userService.updateUser(corrId, sessionService.userId()!!, data)
    true
  }

  @DgsData(field = DgsConstants.SESSION.User, parentType = DgsConstants.SESSION.TYPE_NAME)
  suspend fun userForSession(dfe: DgsDataFetchingEnvironment): User? = coroutineScope {
    val session: Session = dfe.getSource()!!
    session.userId?.let { userService.findById(UUID.fromString(it)).orElseThrow().toDTO() }
  }

  @DgsData(field = DgsConstants.ORDER.User, parentType = DgsConstants.ORDER.TYPE_NAME)
  suspend fun userForOrder(dfe: DgsDataFetchingEnvironment): User = coroutineScope {
    val order: Order = dfe.getSource()!!
    userService.findById(UUID.fromString(order.userId)).orElseThrow().toDTO()
  }
}
