package org.migor.feedless.session

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
abstract class AuthService {
  abstract fun isWhitelisted(request: HttpServletRequest): Boolean
  abstract suspend fun authenticateUser(email: String, secretKey: String): Jwt
  abstract suspend fun findUserById(userId: UserId): User?
  abstract suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret?
  abstract suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime)
}
