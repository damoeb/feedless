package org.migor.feedless.session

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthUserInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.Session
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.context.request.ServletWebRequest

@DgsComponent
@Profile("${AppProfiles.session} & ${AppLayer.api}")
class SessionResolver(
  private val authService: AuthService,
  private val cookieProvider: CookieProvider,
  private val capabilityService: CapabilityService,
) {

  private val log = LoggerFactory.getLogger(SessionResolver::class.simpleName)

  @DgsQuery
  suspend fun session(dfe: DataFetchingEnvironment): Session = withContext(context = createRequestContext()) {
    unsetSessionCookie(dfe)
    val defaultSession = Session(
      isLoggedIn = false,
      isAnonymous = true
    )

    if (capabilityService.hasCapability(UserCapability.ID)) {
      runCatching {
        val userCapability = UserCapability.resolve(capabilityService.getCapability(UserCapability.ID)!!)
        Session(
          isLoggedIn = true,
          isAnonymous = false,
          userId = userCapability.uuid.toString()
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
    @InputArgument(DgsConstants.MUTATION.AUTHUSER_INPUT_ARGUMENT.Data) data: AuthUserInput,
  ): Authentication = withContext(context = createRequestContext()) {
    log.debug("authUser")
    try {
      val jwt = authService.authenticateUser(data.email, data.secretKey)
      addCookie(dfe, cookieProvider.createTokenCookie(jwt))
      Authentication(
        token = jwt.tokenValue,
        corrId = CryptUtil.newCorrId()
      )
    } catch (e: Exception) {
      log.error(e.message, e)
      throw e
    }
  }

  @DgsMutation(field = DgsConstants.MUTATION.Logout)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun logout(
    dfe: DataFetchingEnvironment,
  ): Boolean = withContext(context = createRequestContext()) {
    log.debug("logout")
    val cookie = Cookie("TOKEN", "")
    cookie.isHttpOnly = true
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
