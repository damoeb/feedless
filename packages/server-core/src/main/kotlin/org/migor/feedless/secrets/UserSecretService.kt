package org.migor.feedless.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.userSecret.UserSecretDAO
import org.migor.feedless.data.jpa.userSecret.UserSecretEntity
import org.migor.feedless.data.jpa.userSecret.toDomain
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretType
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.secrets} & ${AppLayer.service} & ${AppLayer.repository}")
class UserSecretService(
  private val userSecretDAO: UserSecretDAO,
  private val jwtTokenIssuer: JwtTokenIssuer
) {

  private val log = LoggerFactory.getLogger(UserSecretService::class.simpleName)

  @Transactional
  suspend fun createUserSecret(user: User): UserSecret {
    val token = jwtTokenIssuer.createJwtForApi(user)
    val k = UserSecretEntity()
    k.ownerId = user.id.uuid
    k.value = token.tokenValue
    k.type = UserSecretType.SecretKey
    k.validUntil = LocalDateTime.ofInstant(
      Instant.ofEpochMilli(
        Clock.System.now().plus(jwtTokenIssuer.getExpiration(AuthTokenType.USER)).toEpochMilliseconds()
      ),
      ZoneId.systemDefault()
    )

    return withContext(Dispatchers.IO) {
      userSecretDAO.save(k).toDomain()
    }
  }

  @Transactional
  suspend fun deleteUserSecret(user: User, uuid: UUID) {
    withContext(Dispatchers.IO) {
      val secret = userSecretDAO.findById(uuid).orElseThrow()
      if (secret.ownerId == user.id.uuid) {
        userSecretDAO.delete(secret)
      } else {
        throw PermissionDeniedException("User does not have an owner")
      }
    }
  }

  @Transactional(readOnly = true)
  suspend fun findBySecretKeyValue(secretKeyValue: String, email: String): UserSecret? {
    return withContext(Dispatchers.IO) {
      userSecretDAO.findBySecretKeyValue(secretKeyValue, email)?.toDomain()
    }
  }

//  @Transactional
//  suspend fun updateLastUsed(id: UUID, date: LocalDateTime) {
//    withContext(Dispatchers.IO) {
//      userSecretDAO.updateLastUsed(id, date)
//    }
//  }

  @Transactional(readOnly = true)
  suspend fun findAllByOwnerId(userId: UserId): List<UserSecret> {
    return withContext(Dispatchers.IO) {
      userSecretDAO.findAllByOwnerId(userId.uuid).map { it.toDomain() }
    }
  }

}
