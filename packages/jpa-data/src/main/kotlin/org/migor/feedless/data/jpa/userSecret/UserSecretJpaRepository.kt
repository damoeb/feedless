package org.migor.feedless.data.jpa.userSecret

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.secrets} & ${AppLayer.repository}")
class UserSecretJpaRepository(private val userSecretDAO: UserSecretDAO) : UserSecretRepository {

  override fun findBySecretKeyValue(
    secretKeyValue: String,
    email: String
  ): UserSecret? {
    return userSecretDAO.findBySecretKeyValue(secretKeyValue, email)?.toDomain()
  }

  override fun existsByValueAndOwnerId(value: String, ownerId: UserId): Boolean {
    return userSecretDAO.existsByValueAndOwnerId(value, ownerId.uuid)
  }

  override fun updateLastUsed(id: UserSecretId, date: LocalDateTime) {
    userSecretDAO.updateLastUsed(id.uuid, date)
  }

  override fun findAllByOwnerId(id: UserId): List<UserSecret> {
    return userSecretDAO.findAllByOwnerId(id.uuid).map { it.toDomain() }
  }

  override fun save(userSecret: UserSecret): UserSecret {
    return userSecretDAO.save(userSecret.toEntity()).toDomain()
  }

  override fun findById(userSecretId: UserSecretId): UserSecret? {
    return userSecretDAO.findById(userSecretId.uuid).getOrNull()?.toDomain()
  }

  override fun deleteById(id: UserSecretId) {
    userSecretDAO.deleteById(id.uuid)
  }

}
