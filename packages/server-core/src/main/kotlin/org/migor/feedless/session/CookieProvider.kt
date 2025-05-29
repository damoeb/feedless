package org.migor.feedless.session

import jakarta.servlet.http.Cookie
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class CookieProvider {
  private val log = LoggerFactory.getLogger(CookieProvider::class.simpleName)

  suspend fun createTokenCookie(jwt: Jwt): Cookie {
    log.debug("creating token cookie")
    val cookie = Cookie("TOKEN", jwt.tokenValue)
    cookie.isHttpOnly = true
    cookie.maxAge =
      LocalDateTime.now().until(LocalDateTime.ofInstant(jwt.expiresAt, ZoneOffset.UTC), ChronoUnit.SECONDS).toInt()
    cookie.secure = false
    cookie.path = "/"
    return cookie
  }

  fun createExpiredSessionCookie(name: String): Cookie {
    val cookie = Cookie(name, "")
    cookie.isHttpOnly = true
    cookie.maxAge = 0
    return cookie
  }
}
