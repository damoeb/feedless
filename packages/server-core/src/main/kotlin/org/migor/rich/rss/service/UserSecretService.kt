package org.migor.rich.rss.service

import org.migor.rich.rss.api.auth.TokenProvider
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.models.UserSecretEntity
import org.migor.rich.rss.data.jpa.models.UserSecretType
import org.migor.rich.rss.data.jpa.repositories.UserSecretDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class UserSecretService {

  private val log = LoggerFactory.getLogger(UserSecretService::class.simpleName)

  @Autowired
  lateinit var userSecretDAO: UserSecretDAO

  @Autowired
  lateinit var tokenProvider: TokenProvider

  fun createSecretKey(secretKey: String, expiresIn: Duration, user: UserEntity): UserSecretEntity {
    val k = UserSecretEntity()
    k.ownerId = user.id
    k.value = secretKey
    k.type = UserSecretType.SecretKey
    k.validUntil = Date.from(LocalDateTime.now().plus(expiresIn).atZone(ZoneId.systemDefault()).toInstant())

    return userSecretDAO.save(k)
  }

  fun createApiToken(corrId: String, user: UserEntity): UserSecretEntity {
    val token = tokenProvider.createJwtForApi(user)
    val k = UserSecretEntity()
    k.ownerId = user.id
    k.value = token.tokenValue
    k.type = UserSecretType.SecretKey
    k.validUntil = Date.from(LocalDateTime.now().plus(tokenProvider.getApiTokenExpiration()).atZone(ZoneId.systemDefault()).toInstant())

    return userSecretDAO.save(k)
  }

  fun deleteApiTokens(corrId: String, user: UserEntity, uuids: List<UUID>) {
    userSecretDAO.deleteAllByIdAndOwnerId(uuids, user.id)
  }

}
