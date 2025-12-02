package org.migor.feedless.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.migor.feedless.userSecret.UserSecretType
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Service
@Profile("${AppProfiles.secrets} & ${AppLayer.service} & ${AppLayer.repository}")
class UserSecretUseCase(
  private val userSecretRepository: UserSecretRepository,
  private val jwtTokenIssuer: JwtTokenIssuer
) {

  private val log = LoggerFactory.getLogger(UserSecretUseCase::class.simpleName)

  suspend fun createUserSecret(user: User): UserSecret {
    val token = jwtTokenIssuer.createJwtForApi(user)
    val k = UserSecret(
      ownerId = user.id,
      value = token.tokenValue,
      type = UserSecretType.SecretKey,
      validUntil = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(
          Clock.System.now().plus(jwtTokenIssuer.getExpiration(AuthTokenType.USER)).toEpochMilliseconds()
        ),
        ZoneId.systemDefault()
      )
    )

    return withContext(Dispatchers.IO) {
      userSecretRepository.save(k)
    }
  }

  suspend fun deleteUserSecret(user: User, userSecretId: UserSecretId) {
    withContext(Dispatchers.IO) {
      val secret = userSecretRepository.findById(userSecretId)!!
      if (secret.ownerId == user.id) {
        userSecretRepository.deleteById(secret.id)
      } else {
        throw PermissionDeniedException("User does not have an owner")
      }
    }
  }

//  @Transactional
//  suspend fun updateLastUsed(id: UUID, date: LocalDateTime) {
//    withContext(Dispatchers.IO) {
//      userSecretDAO.updateLastUsed(id, date)
//    }
//  }

}
