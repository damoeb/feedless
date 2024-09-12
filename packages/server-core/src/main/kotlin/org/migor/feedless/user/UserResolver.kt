package org.migor.feedless.user

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.ConnectedApp
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.Session
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.toMillis
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

  @Autowired
  private lateinit var connectedAppService: ConnectedAppService

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateCurrentUser)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun updateCurrentUser(
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpdateCurrentUserInput,
  ): Boolean = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.info("[$corrId] updateCurrentUser ${sessionService.userId()} $data")
    userService.updateUser(corrId, sessionService.userId()!!, data)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateConnectedApp)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun updateConnectedApp(
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument id: String,
    @InputArgument authorize: Boolean,
  ): Boolean = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.info("[$corrId] updateConnectedApp ${sessionService.userId()}")
    userService.updateConnectedApp(corrId, sessionService.userId()!!, id, authorize)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteConnectedApp)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteConnectedApp(
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument id: String,
  ): Boolean = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.info("[$corrId] deleteConnectedApp ${sessionService.userId()}")
    userService.deleteConnectedApp(corrId, sessionService.userId()!!, id)
    true
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.ConnectedApp)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun connectedApp(
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument id: String,
  ): ConnectedApp = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.info("[$corrId] connectedApp ${sessionService.userId()} ")
    userService.getConnectedAppByUserAndId(corrId, sessionService.userId()!!, id).toDto()
  }

  @DgsData(field = DgsConstants.SESSION.User, parentType = DgsConstants.SESSION.TYPE_NAME)
  suspend fun userForSession(dfe: DgsDataFetchingEnvironment): User? = coroutineScope {
    val session: Session = dfe.getSource()!!
    session.userId?.let { userService.findById(UUID.fromString(it)).orElseThrow().toDTO() }
  }


  @DgsData(field = DgsConstants.USER.ConnectedApps, parentType = DgsConstants.USER.TYPE_NAME)
  suspend fun connectedApps(dfe: DgsDataFetchingEnvironment): List<ConnectedApp> = coroutineScope {
    val user: User = dfe.getSource()!!
    connectedAppService.findAllByUserId(UUID.fromString(user.id)).filterIsInstance<TelegramConnectionEntity>()
      .map { it.toDto() }
  }

  @DgsData(field = DgsConstants.ORDER.User, parentType = DgsConstants.ORDER.TYPE_NAME)
  suspend fun userForOrder(dfe: DgsDataFetchingEnvironment): User = coroutineScope {
    val order: Order = dfe.getSource()!!
    userService.findById(UUID.fromString(order.userId)).orElseThrow().toDTO()
  }
}

private fun ConnectedAppEntity.toDto(): ConnectedApp {
  return ConnectedApp(
    id = id.toString(),
    authorized = authorized,
    authorizedAt = authorizedAt?.toMillis(),
    app = when (this) {
      is TelegramConnectionEntity -> "Telegram"
      is GithubConnectionEntity -> "Github"
      else -> "Unknown"
    }
  )
}
