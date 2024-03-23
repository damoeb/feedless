package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.auth.CookieProvider
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.api.auth.MailAuthenticationService
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.data.jpa.models.fromDto
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.generated.types.AuthUserInput
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.CreateUserInput
import org.migor.feedless.generated.types.DeleteUserSecretsInput
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.generated.types.SourceSubscriptionUniqueWhereInput
import org.migor.feedless.generated.types.SourceSubscriptionUpdateInput
import org.migor.feedless.generated.types.SourceSubscriptionsCreateInput
import org.migor.feedless.generated.types.SubmitAgentDataInput
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.generated.types.UserSecret
import org.migor.feedless.service.AgentService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.SourceSubscriptionService
import org.migor.feedless.service.UserSecretService
import org.migor.feedless.service.UserService
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest
import java.util.*
import org.migor.feedless.generated.types.Authentication as AuthenticationDto

@DgsComponent
@Profile(AppProfiles.database)
class MutationResolver {

  private val log = LoggerFactory.getLogger(MutationResolver::class.simpleName)

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Autowired
  lateinit var agentService: AgentService

  @Autowired
  lateinit var cookieProvider: CookieProvider

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var sourceSubscriptionService: SourceSubscriptionService

  @Autowired
  lateinit var userSecretService: UserSecretService

  @Autowired
  lateinit var currentUser: CurrentUser

  private fun addCookie(dfe: DataFetchingEnvironment, cookie: Cookie) {
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(
      cookie
    )
  }

  @Throttled
  @DgsMutation
  suspend fun authUser(
    @RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
    dfe: DataFetchingEnvironment,
    @InputArgument data: AuthUserInput,
  ): AuthenticationDto = coroutineScope {
    val corrId = handleCorrId(corrIdParam)
    log.info("[$corrId] authUser")
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.authRoot))) {
      log.info("[$corrId] authRoot")
      val root = userService.findByEmail(data.email) ?: throw NotFoundException("user not found ($corrId)")
      if (!root.root) {
        throw PermissionDeniedException("account is not root ($corrId)")
      }
      userSecretService.findBySecretKeyValue(data.secretKey, data.email)
        ?: throw IllegalArgumentException("secretKey does not match ($corrId)")
      val jwt = tokenProvider.createJwtForUser(root)
      addCookie(dfe, cookieProvider.createTokenCookie(corrId, jwt))
      AuthenticationDto.newBuilder()
        .token(jwt.tokenValue)
        .corrId(CryptUtil.newCorrId())
        .build()
    } else {
      throw PermissionDeniedException("authRoot profile is not active ($corrId)")
    }
  }

  @Throttled
  @DgsMutation
  suspend fun authConfirmCode(
    @InputArgument data: ConfirmAuthCodeInput,
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] authConfirmCode")
    mailAuthenticationService.confirmAuthCode(corrId, data, resolveHttpResponse(dfe))
    true
  }

  private fun resolveHttpResponse(dfe: DataFetchingEnvironment): HttpServletResponse {
    return ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!
  }


  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('PROVIDE_HTTP_RESPONSE')")
  suspend fun submitAgentData(@InputArgument data: SubmitAgentDataInput): Boolean = coroutineScope {
    log.info("[${data.corrId}] submitAgentData")
    agentService.handleScrapeResponse(data.corrId, data.jobId, data.scrapeResponse)
    true
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createUserSecret(
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): UserSecret = coroutineScope {
    userSecretService.createUserSecret(corrId, currentUser.user(corrId)).toDto(false)
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createUser(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: CreateUserInput
  ): User = coroutineScope {
    log.info("[$corrId] createUser $data")
    userService.createUser(
      corrId,
      email = data.email,
      productName = data.product.fromDto(),
      AuthSource.email,
      planName = data.plan.fromDto()
    ).toDto()
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteUserSecrets(
    @InputArgument data: DeleteUserSecretsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    userSecretService.deleteUserSecrets(corrId, currentUser.user(corrId), data.where.`in`.map { UUID.fromString(it) })
    true
  }


  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateCurrentUser(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpdateCurrentUserInput,
  ): Boolean = coroutineScope {
    log.info("[$corrId] updateCurrentUser ${currentUser.userId()} $data")
    userService.updateUser(corrId, currentUser.userId()!!, data)
    true
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createSourceSubscriptions(
    @InputArgument("data") data: SourceSubscriptionsCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<SourceSubscription> = coroutineScope {
    log.info("[$corrId] createSourceSubscriptions $data")
    sourceSubscriptionService.create(corrId, data)
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateSourceSubscription(
    @InputArgument("data") data: SourceSubscriptionUpdateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): SourceSubscription = coroutineScope {
    log.info("[$corrId] updateSourceSubscription $data")
    sourceSubscriptionService.update(corrId, UUID.fromString(data.where.id), data.data).toDto()
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteSourceSubscription(
    @InputArgument("data") data: SourceSubscriptionUniqueWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] deleteSourceSubscription $data")
    sourceSubscriptionService.delete(corrId, UUID.fromString(data.id))
    true
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  suspend fun logout(
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] logout")
    val cookie = Cookie("TOKEN", "")
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = 0
    addCookie(dfe, cookie)
    true
  }
}
