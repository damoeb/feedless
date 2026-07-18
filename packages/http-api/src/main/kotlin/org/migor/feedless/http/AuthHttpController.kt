package org.migor.feedless.http

import jakarta.servlet.http.Cookie
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.http.api.AuthApi
import org.migor.feedless.http.api.model.AuthUserRequest
import org.migor.feedless.http.api.model.AuthenticationResponse
import org.migor.feedless.http.api.model.SessionResponse
import org.migor.feedless.http.mapper.HttpAuthMapper
import org.migor.feedless.session.HttpSetCookie
import org.migor.feedless.session.SessionTokenPort
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.util.CryptUtil
import org.slf4j.MDC
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.session} & ${AppLayer.api}")
class AuthHttpController(
  private val sessionTokenPort: SessionTokenPort,
  private val httpAuthMapper: HttpAuthMapper,
) : AuthApi {

  override suspend fun getSession(): ResponseEntity<SessionResponse> {
    val session = sessionTokenPort.getCurrentSession()
    return ResponseEntity.ok(httpAuthMapper.toSessionResponse(session))
  }

  @Throttled
  override suspend fun login(authUserRequest: AuthUserRequest): ResponseEntity<AuthenticationResponse> {
    val corrId = CryptUtil.newCorrId()
    MDC.put("corrId", corrId)
    try {
      val token = sessionTokenPort.authenticateUser(authUserRequest.email, authUserRequest.secretKey)
      addCookie(sessionTokenPort.toCookie(token))
      return ResponseEntity.ok(httpAuthMapper.toAuthenticationResponse(token, corrId))
    } finally {
      MDC.remove("corrId")
    }
  }

  @Throttled
  override suspend fun logout(): ResponseEntity<Unit> {
    addCookie(sessionTokenPort.createExpiredTokenCookie())
    return ResponseEntity.noContent().build()
  }

  private fun addCookie(cookie: HttpSetCookie) {
    val response = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).response!!
    response.addCookie(
      Cookie(cookie.name, cookie.value).apply {
        isHttpOnly = cookie.httpOnly
        maxAge = cookie.maxAge
        secure = cookie.secure
        path = cookie.path
      },
    )
  }
}
