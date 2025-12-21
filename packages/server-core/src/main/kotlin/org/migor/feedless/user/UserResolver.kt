package org.migor.feedless.user

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.connectedApp.ConnectedApp
import org.migor.feedless.connectedApp.ConnectedAppId
import org.migor.feedless.connectedApp.GithubConnection
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.session.createRequestContext
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.migor.feedless.generated.types.ConnectedApp as ConnectedAppDto
import org.migor.feedless.generated.types.Feature as FeatureDto
import org.migor.feedless.generated.types.Order as OrderDto
import org.migor.feedless.generated.types.Session as SessionDto
import org.migor.feedless.generated.types.User as UserDto

@DgsComponent
@Profile("${AppProfiles.user} & ${AppLayer.api}")
class UserResolver(
  private val userUseCase: UserUseCase,
  private val userRepository: UserRepository,
  private val connectedAppUseCase: ConnectedAppUseCase,
  private val capabilityService: CapabilityService
) {

  private val log = LoggerFactory.getLogger(UserResolver::class.simpleName)

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateCurrentUser)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun updateCurrentUser(
    dfe: DataFetchingEnvironment,
    @InputArgument data: UpdateCurrentUserInput,
  ): Boolean = withContext(context = createRequestContext()) {
    log.info("updateCurrentUser ${userId()} $data")
    userUseCase.updateUser(userId()!!, data)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.UpdateConnectedApp)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun updateConnectedApp(
    dfe: DataFetchingEnvironment,
    @InputArgument id: String,
    @InputArgument authorize: Boolean,
  ): Boolean = withContext(context = createRequestContext()) {
    log.info("updateConnectedApp ${userId()}")
    userUseCase.updateConnectedApp(userId()!!, ConnectedAppId(id), authorize)
    true
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteConnectedApp)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun deleteConnectedApp(
    dfe: DataFetchingEnvironment,
    @InputArgument id: String,
  ): Boolean = withContext(context = createRequestContext()) {
    log.info("deleteConnectedApp ${userId()}")
    userUseCase.deleteConnectedApp(userId()!!, ConnectedAppId(id))
    true
  }

  private fun userId(): UserId? {
    return capabilityService.getCapability(UserCapability.ID)?.let { UserCapability.resolve(it) }
  }

  @Throttled
  @DgsQuery(field = DgsConstants.QUERY.ConnectedApp)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun getConnectedApp(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.CONNECTEDAPP_INPUT_ARGUMENT.Id) id: String,
  ): ConnectedAppDto = withContext(context = createRequestContext()) {
    log.info("connectedApp ${userId()} ")
    userUseCase.getConnectedAppByUserAndId(userId()!!, ConnectedAppId(id)).toDto()
  }

  @DgsData(field = DgsConstants.SESSION.User, parentType = DgsConstants.SESSION.TYPE_NAME)
  suspend fun getUserForSession(dfe: DgsDataFetchingEnvironment): UserDto? =
    coroutineScope {
      val session: SessionDto = dfe.getSourceOrThrow()
      session.userId?.let { withContext(Dispatchers.IO) { userRepository.findById(UserId(it))!!.toDto() } }
    }


  @DgsData(field = DgsConstants.USER.ConnectedApps, parentType = DgsConstants.USER.TYPE_NAME)
  suspend fun getConnectedApps(dfe: DgsDataFetchingEnvironment): List<ConnectedAppDto> =
    coroutineScope {
      val user: UserDto = dfe.getSourceOrThrow()
      connectedAppUseCase.findAllByUserId(UserId(user.id)).filterIsInstance<TelegramConnection>()
        .map { it.toDto() }
    }

  @DgsData(field = DgsConstants.USER.Features, parentType = DgsConstants.USER.TYPE_NAME)
  suspend fun getFeatures(dfe: DgsDataFetchingEnvironment): List<FeatureDto> =
    coroutineScope {
      val user: UserDto = dfe.getSourceOrThrow()

//  todo fix this  featureService.findAllByProductAndUserId(Vertical.feedless, UserId(user.id)).map { it.toDto() }
      emptyList()
    }

  @DgsData(field = DgsConstants.ORDER.User, parentType = DgsConstants.ORDER.TYPE_NAME)
  suspend fun userForOrder(dfe: DgsDataFetchingEnvironment): UserDto = coroutineScope {
    val order: OrderDto = dfe.getSourceOrThrow()
    withContext(Dispatchers.IO) {
      userRepository.findById(UserId(order.userId))!!.toDto()
    }
  }
}

private fun ConnectedApp.toDto(): ConnectedAppDto {
  return ConnectedAppDto(
    id = id.toString(),
    authorized = authorized,
    authorizedAt = authorizedAt?.toMillis(),
    app = when (this) {
      is TelegramConnection -> "Telegram"
      is GithubConnection -> "Github"
      else -> "Unknown"
    }
  )
}
