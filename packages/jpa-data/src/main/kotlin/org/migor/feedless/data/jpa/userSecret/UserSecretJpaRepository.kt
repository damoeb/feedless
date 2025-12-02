package org.migor.feedless.data.jpa.userSecret

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("${AppProfiles.secrets} & ${AppLayer.repository}")
class UserSecretJpaRepository(private val userSecretDAO: UserSecretDAO) : UserSecretRepository {

  override suspend fun findBySecretKeyValue(
    secretKeyValue: String,
    email: String
  ): UserSecret? {
    return withContext(Dispatchers.IO) {
      userSecretDAO.findBySecretKeyValue(secretKeyValue, email)?.toDomain()
    }
  }

  override suspend fun existsByValueAndOwnerId(value: String, ownerId: UserId): Boolean {
    return withContext(Dispatchers.IO) {
      userSecretDAO.existsByValueAndOwnerId(value, ownerId.uuid)
    }
  }

  override suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime) {
    withContext(Dispatchers.IO) {
      userSecretDAO.updateLastUsed(id.uuid, date)
    }
  }

  override suspend fun findAllByOwnerId(id: UserSecretId): List<UserSecret> {
    return withContext(Dispatchers.IO) {
      userSecretDAO.findAllByOwnerId(id.uuid).map { it.toDomain() }
    }
  }

  override suspend fun save(userSecret: UserSecret): UserSecret {
    return withContext(Dispatchers.IO) {
      userSecretDAO.save(userSecret.toEntity()).toDomain()
    }
  }

}
