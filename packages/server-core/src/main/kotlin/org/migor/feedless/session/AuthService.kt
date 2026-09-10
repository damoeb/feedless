package org.migor.feedless.session

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
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

  /**
   * Trägt die Report-Id in Bestätigungs- und Abmeldelinks. Der Besitz des
   * signierten Tokens ist dort der Nachweis - die Empfänger sind
   * typischerweise nicht angemeldet.
   */
  const val REPORT_ID = "report_id"
}

enum class AuthTokenType(val value: String) {
  ANONYMOUS("ANON"),
  USER("USER"),
  API("API"),
  SERVICE("AGENT"),
}

@Service
abstract class AuthService {
  abstract fun isWhitelisted(request: HttpServletRequest): Boolean
  abstract suspend fun authenticateUser(email: String, secretKey: String): Jwt
  abstract suspend fun findUserById(userId: UserId): User?
  abstract suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret?
  abstract suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime)
}
