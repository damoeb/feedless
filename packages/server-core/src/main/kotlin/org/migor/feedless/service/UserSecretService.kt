package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.models.UserSecretEntity
import org.migor.feedless.data.jpa.models.UserSecretType
import org.migor.feedless.data.jpa.repositories.UserSecretDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@Profile(AppProfiles.database)
class UserSecretService {

  private val log = LoggerFactory.getLogger(UserSecretService::class.simpleName)

  @Autowired
  lateinit var userSecretDAO: UserSecretDAO

  @Autowired
  lateinit var tokenProvider: TokenProvider

//  fun createSecretKey(secretKey: String, expiresIn: Duration, user: UserEntity): UserSecretEntity {
//    val k = UserSecretEntity()
//    k.ownerId = user.id
//    k.value = secretKey
//    k.type = UserSecretType.SecretKey
//    k.validUntil = Date.from(LocalDateTime.now().plus(expiresIn).atZone(ZoneId.systemDefault()).toInstant())
//
//    return userSecretDAO.save(k)
//  }

  fun createUserSecret(corrId: String, user: UserEntity): UserSecretEntity {
    val token = tokenProvider.createJwtForApi(user)
    val k = UserSecretEntity()
    k.ownerId = user.id
    k.value = token.tokenValue
    k.type = UserSecretType.SecretKey
    k.validUntil = Date.from(
      LocalDateTime.now().plus(tokenProvider.getApiTokenExpiration()).atZone(ZoneId.systemDefault()).toInstant()
    )

    return userSecretDAO.save(k)
  }

  fun deleteUserSecrets(corrId: String, user: UserEntity, uuids: List<UUID>) {
    userSecretDAO.deleteAllByIdAndOwnerId(uuids, user.id)
  }

  fun findBySecretKeyValue(secretKeyValue: String, email: String): UserSecretEntity? {
    return userSecretDAO.findBySecretKeyValue(secretKeyValue, email)
  }

  fun updateLastUsed(id: UUID, date: Date) {
    userSecretDAO.updateLastUsed(id, date)
  }

}
