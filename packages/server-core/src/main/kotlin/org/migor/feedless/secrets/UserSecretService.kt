package org.migor.feedless.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.time.toJavaDuration

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.secrets} & ${AppLayer.service} & ${AppLayer.repository}")
class UserSecretService(
  private val userSecretDAO: UserSecretDAO,
  private val tokenProvider: TokenProvider
) {

  private val log = LoggerFactory.getLogger(UserSecretService::class.simpleName)

  @Transactional
  suspend fun createUserSecret(user: UserEntity): UserSecretEntity {
    val token = tokenProvider.createJwtForApi(user)
    val k = UserSecretEntity()
    k.ownerId = user.id
    k.value = token.tokenValue
    k.type = UserSecretType.SecretKey
    k.validUntil = LocalDateTime.now().plus(tokenProvider.getExpiration(AuthTokenType.USER).toJavaDuration())

    return withContext(Dispatchers.IO) {
      userSecretDAO.save(k)
    }
  }

  @Transactional
  suspend fun deleteUserSecret(user: UserEntity, uuid: UUID) {
    withContext(Dispatchers.IO) {
      val secret = userSecretDAO.findById(uuid).orElseThrow()
      if (secret.ownerId == user.id) {
        userSecretDAO.delete(secret)
      } else {
        throw PermissionDeniedException("User does not have an owner")
      }
    }
  }

  @Transactional(readOnly = true)
  suspend fun findBySecretKeyValue(secretKeyValue: String, email: String): UserSecretEntity? {
    return withContext(Dispatchers.IO) {
      userSecretDAO.findBySecretKeyValue(secretKeyValue, email)
    }
  }

  @Transactional
  suspend fun updateLastUsed(id: UUID, date: LocalDateTime) {
    withContext(Dispatchers.IO) {
      userSecretDAO.updateLastUsed(id, date)
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByOwnerId(userId: UUID): List<UserSecretEntity> {
    return withContext(Dispatchers.IO) {
      userSecretDAO.findAllByOwnerId(userId)
    }
  }

}
