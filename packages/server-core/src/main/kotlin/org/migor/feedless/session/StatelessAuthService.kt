package org.migor.feedless.session

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
@ConditionalOnMissingBean(StatefulAuthService::class)
class StatelessAuthService : AuthService() {
  @Value("\${app.rootEmail}")
  private lateinit var rootEmail: String

  @Value("\${app.rootSecretKey}")
  private lateinit var rootSecretKey: String

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  private lateinit var root: User
  private lateinit var key: UserSecret


  @PostConstruct
  fun init() {
    root = User(
      email = rootEmail,
      lastLogin = LocalDateTime.now(),
      hasAcceptedTerms = true,
    )
    key = UserSecret(
      value = rootSecretKey,
      validUntil = LocalDateTime.now().plusDays(1),
      type = UserSecretType.SecretKey,
      ownerId = root.id,
    )
  }

  override suspend fun parseAndVerify(token: String): Jwt? = null
  override suspend fun assertToken(request: HttpServletRequest) {

  }

  override fun isWhitelisted(request: HttpServletRequest): Boolean = true

  override suspend fun interceptToken(request: HttpServletRequest): Jwt {
    TODO("ignore")
  }

  override suspend fun authenticateUser(email: String, secretKey: String): Jwt {
    return if (email == rootEmail && secretKey == rootSecretKey) {
      jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(root.id)))
    } else {
      throw AccessDeniedException("User does not exist or password invalid")
    }
  }

  override suspend fun findUserById(userId: UserId): User? {
    return if (root.id == userId.uuid) {
      root
    } else {
      null
    }
  }

  override suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret? {
    return if (email == rootEmail && secretKey == rootSecretKey) {
      key
    } else {
      throw AccessDeniedException("User does not exist or password invalid")
    }
  }
}
