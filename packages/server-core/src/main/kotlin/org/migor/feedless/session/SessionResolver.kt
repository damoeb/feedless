package org.migor.feedless.session

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.common.PropertyService
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthUserInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.Session
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.user.UserService
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.ServletWebRequest

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.session} & ${AppLayer.api}")
class SessionResolver {

  private val log = LoggerFactory.getLogger(SessionResolver::class.simpleName)

  @Autowired
  private lateinit var tokenProvider: TokenProvider

  @Autowired
  private lateinit var environment: Environment

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var userSecretService: UserSecretService

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var cookieProvider: CookieProvider

  @DgsQuery
  suspend fun session(dfe: DataFetchingEnvironment): Session =
    withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
      unsetSessionCookie(dfe)
      val defaultSession = Session(
        isLoggedIn = false,
        isAnonymous = true
      )

      if (sessionService.isUser()) {
        runCatching {
          val user = sessionService.user()
          Session(
            isLoggedIn = true,
            isAnonymous = false,
            userId = user.id.toString()
          )
        }.getOrDefault(defaultSession)
      } else {
        defaultSession

      }
    }

  private fun addCookie(dfe: DataFetchingEnvironment, cookie: Cookie) {
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(
      cookie
    )
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.AuthUser)
  suspend fun authUser(
    dfe: DataFetchingEnvironment,
    @InputArgument data: AuthUserInput,
  ): Authentication = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("authUser")
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.authRoot))) {
      log.debug("authRoot")
      val root = userService.findByEmail(data.email) ?: throw NotFoundException("user not found")
      if (!root.admin) {
        throw PermissionDeniedException("account is not root")
      }
      userSecretService.findBySecretKeyValue(data.secretKey, data.email)
        ?: throw IllegalArgumentException("secretKey does not match")
      val jwt = tokenProvider.createJwtForUser(root)
      addCookie(dfe, cookieProvider.createTokenCookie(jwt))
      Authentication(
        token = jwt.tokenValue,
        corrId = CryptUtil.newCorrId()
      )
    } else {
      throw PermissionDeniedException("authRoot profile is not active")
    }
  }

  @DgsMutation(field = DgsConstants.MUTATION.Logout)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun logout(
    dfe: DataFetchingEnvironment,
  ): Boolean = coroutineScope {
    log.debug("logout")
    val cookie = Cookie("TOKEN", "")
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = 0
    addCookie(dfe, cookie)
    true
  }

  private fun unsetSessionCookie(dfe: DataFetchingEnvironment) {
    val cookie = cookieProvider.createExpiredSessionCookie("JSESSION")
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(
      cookie
    )
  }
}
