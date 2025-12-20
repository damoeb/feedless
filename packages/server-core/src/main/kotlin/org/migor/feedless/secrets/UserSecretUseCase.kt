package org.migor.feedless.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.userId
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
  private val userRepository: UserRepository,
  private val jwtTokenIssuer: JwtTokenIssuer
) {

  private val log = LoggerFactory.getLogger(UserSecretUseCase::class.simpleName)

  suspend fun createUserSecret(): UserSecret = withContext(Dispatchers.IO) {
    log.info("createUserSecret")
    val userId = coroutineContext.userId()
    val user = userRepository.findById(userId)!!
    val token = jwtTokenIssuer.createJwtForApi(user)

    userSecretRepository.save(
      UserSecret(
        ownerId = userId,
        value = token.tokenValue,
        type = UserSecretType.SecretKey,
        validUntil = LocalDateTime.ofInstant(
          Instant.ofEpochMilli(
            Clock.System.now().plus(jwtTokenIssuer.getExpiration(AuthTokenType.USER)).toEpochMilliseconds()
          ),
          ZoneId.systemDefault()
        )
      )
    )
  }

  suspend fun deleteUserSecret(userSecretId: UserSecretId) = withContext(Dispatchers.IO) {
    log.info("deleteUserSecret userSecretId=$userSecretId")
    val secret = userSecretRepository.findById(userSecretId)!!
    if (secret.ownerId == coroutineContext.userId()) { // todo should be group
      userSecretRepository.deleteById(secret.id)
    } else {
      throw PermissionDeniedException("User does not have an owner")
    }
  }
}
