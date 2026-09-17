package org.migor.feedless.session

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
@ConditionalOnMissingBean(StatefulAuthService::class)
class StatelessAuthService : AuthService() {
  @Autowired
  private lateinit var rootUserProperties: RootUserProperties

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  private lateinit var root: User
  private lateinit var key: UserSecret


  @PostConstruct
  fun init() {
    root = User(
      email = rootUserProperties.rootEmail,
      lastLogin = LocalDateTime.now(),
      hasAcceptedTerms = true,
    )
    key = UserSecret(
      name = "Root secret key",
      value = rootUserProperties.rootSecretKey,
      validUntil = LocalDateTime.now().plusDays(1),
      type = UserSecretType.SecretKey,
      ownerId = root.id,
    )
  }

  override fun isWhitelisted(request: HttpServletRequest): Boolean = true

  override suspend fun authenticateUser(email: String, secretKey: String): Jwt {
    return if (email == rootUserProperties.rootEmail && secretKey == rootUserProperties.rootSecretKey) {
      jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(root.id)))
    } else {
      throw AccessDeniedException("User does not exist or password invalid")
    }
  }

  override suspend fun findUserById(userId: UserId): User? {
    return if (root.id == userId) {
      root
    } else {
      null
    }
  }

  override suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret? {
    return if (email == rootUserProperties.rootEmail && secretKey == rootUserProperties.rootSecretKey) {
      key
    } else {
      throw AccessDeniedException("User does not exist or password invalid")
    }
  }

  override suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime) {
    // ignore
  }

  // Stateless mode stores no secrets, so no API token can be in use.
  override fun useApiSecret(id: UserSecretId, ownerId: UserId, now: LocalDateTime): Boolean = false
}
