package org.migor.feedless.session

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class SessionTokenPortAdapter(
  private val authService: AuthService,
  private val cookieProvider: CookieProvider,
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val capabilityService: CapabilityService,
) : SessionTokenPort {

  override suspend fun authenticateUser(email: String, secretKey: String): AuthToken {
    try {
      val jwt = authService.authenticateUser(email, secretKey)
      return AuthToken(jwt.tokenValue)
    } catch (e: NotFoundException) {
      throw AuthUserNotFoundException(e.message ?: "user not found")
    } catch (e: IllegalArgumentException) {
      throw AuthCredentialsException(e.message ?: "invalid credentials")
    }
  }

  override suspend fun toCookie(token: AuthToken): HttpSetCookie {
    val jwt = jwtTokenIssuer.decodeJwt(token.token)
    val cookie = cookieProvider.createTokenCookie(jwt)
    return HttpSetCookie(
      name = cookie.name,
      value = cookie.value,
      httpOnly = cookie.isHttpOnly,
      maxAge = cookie.maxAge,
      secure = cookie.secure,
      path = cookie.path,
    )
  }

  override fun createExpiredTokenCookie(name: String): HttpSetCookie {
    val cookie = cookieProvider.createExpiredSessionCookie(name)
    return HttpSetCookie(
      name = cookie.name,
      value = cookie.value,
      httpOnly = cookie.isHttpOnly,
      maxAge = cookie.maxAge,
      secure = cookie.secure,
      path = cookie.path,
    )
  }

  override suspend fun getCurrentSession(): SessionInfo {
    if (!capabilityService.hasCapability(UserCapability.ID)) {
      return SessionInfo(isLoggedIn = false, isAnonymous = true)
    }
    val userId = UserCapability.resolve(capabilityService.getCapability(UserCapability.ID)!!)
    return SessionInfo(
      isLoggedIn = true,
      isAnonymous = false,
      userId = userId,
    )
  }
}
