package org.migor.rich.rss.api.auth

import jakarta.servlet.http.Cookie
import org.migor.rich.rss.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service


@Service
class CookieProvider {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var tokenProvider: TokenProvider

  fun createTokenCookie(authToken: Jwt): Cookie {
    val cookie = Cookie("TOKEN", authToken.tokenValue)
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = tokenProvider.getUserTokenExpiration().seconds.toInt()
    cookie.secure = true
    cookie.path = "/"
    return cookie
  }

  fun createExpiredSessionCookie(name: String): Cookie {
    val cookie = Cookie(name, "")
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = 0
    return cookie
  }
}
