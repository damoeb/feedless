package org.migor.feedless.session

import org.migor.feedless.auth.AuthToken

interface SessionTokenPort {
  suspend fun authenticateUser(email: String, secretKey: String): AuthToken

  suspend fun toCookie(token: AuthToken): HttpSetCookie

  fun createExpiredTokenCookie(name: String = "TOKEN"): HttpSetCookie

  suspend fun getCurrentSession(): SessionInfo
}
