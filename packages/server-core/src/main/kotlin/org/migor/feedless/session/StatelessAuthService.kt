package org.migor.feedless.session

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(propagation = Propagation.NEVER)
@ConditionalOnMissingBean(StatefulAuthService::class)
class StatelessAuthService : AuthService() {
  @Value("\${app.rootEmail}")
  private lateinit var rootEmail: String

  @Value("\${app.rootSecretKey}")
  private lateinit var rootSecretKey: String

  private val root = UserEntity()
  private val key = UserSecretEntity()

  override suspend fun verifyTokenSignature(token: String): Jwt? = null
  override suspend fun assertToken(request: HttpServletRequest) {

  }

  override fun isWhitelisted(request: HttpServletRequest): Boolean = true

  override suspend fun interceptToken(request: HttpServletRequest): Jwt {
    TODO("Not yet implemented")
  }

  override fun authenticateUser(email: String, secretKey: String): UserEntity {
    return if (email == rootEmail && secretKey == rootSecretKey) {
      root
    } else {
      throw AccessDeniedException("User does not exist or password invalid")
    }
  }

  override suspend fun findUserById(userId: UserId): UserEntity? {
    return if (root.id == userId.value) {
      root
    } else {
      null
    }
  }

  override suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecretEntity? {
    return if (email == rootEmail && secretKey == rootSecretKey) {
      key
    } else {
      throw AccessDeniedException("User does not exist or password invalid")
    }
  }
}
