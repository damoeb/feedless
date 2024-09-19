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
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.ConnectedApp
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.Session
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
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
    @InputArgument data: UpdateCurrentUserInput,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.info("updateCurrentUser ${coroutineContext.userId()} $data")
    userService.updateUser(coroutineContext.userId(), data)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateConnectedApp)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun updateConnectedApp(
    dfe: DataFetchingEnvironment,
    @InputArgument id: String,
    @InputArgument authorize: Boolean,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.info("updateConnectedApp ${coroutineContext.userId()}")
    userService.updateConnectedApp(coroutineContext.userId(), UUID.fromString(id), authorize)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteConnectedApp)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteConnectedApp(
    dfe: DataFetchingEnvironment,
    @InputArgument id: String,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.info("deleteConnectedApp ${coroutineContext.userId()}")
    userService.deleteConnectedApp(coroutineContext.userId(), UUID.fromString(id))
    true
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.ConnectedApp)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun connectedApp(
    dfe: DataFetchingEnvironment,
    @InputArgument id: String,
  ): ConnectedApp = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.info("connectedApp ${coroutineContext.userId()} ")
    userService.getConnectedAppByUserAndId(coroutineContext.userId(), UUID.fromString(id)).toDto()
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
