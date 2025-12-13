package org.migor.feedless.session

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.LocalDateTime

object JwtParameterNames {
  const val EXP = "exp"
  const val ID = "id"
  const val IAT = "iat"

  @Deprecated("will be removed")
  const val USER_ID = "user_id"
  const val CAPABILITIES = "capabilities"
  const val TYPE = "token_type"
  const val HOST = "host"
}

enum class AuthTokenType(val value: String) {
  ANONYMOUS("ANON"),
  USER("USER"),
  API("API"),
  SERVICE("AGENT"),
}

@Service
abstract class AuthService {
  abstract suspend fun parseAndVerify(token: String): Jwt?
  abstract suspend fun assertToken(request: HttpServletRequest)
  abstract fun isWhitelisted(request: HttpServletRequest): Boolean
  abstract suspend fun interceptToken(request: HttpServletRequest): Jwt
  abstract suspend fun authenticateUser(email: String, secretKey: String): Jwt

  @Throws(AccessDeniedException::class)
  protected fun interceptTokenRaw(request: HttpServletRequest): String {
    val authCookie = request.cookies?.firstOrNull { it.name == "TOKEN" }
    if (StringUtils.isNotBlank(authCookie?.value)) {
      return authCookie?.value!!
    }
    val authHeader = request.getHeader("Authentication")
    if (StringUtils.isNotBlank(authHeader)) {
      return authHeader.replaceFirst("Bearer ", "")
    }
    throw AccessDeniedException("token not present")
  }

  abstract suspend fun findUserById(userId: UserId): User?
  abstract suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret?
  suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime) {}
}
