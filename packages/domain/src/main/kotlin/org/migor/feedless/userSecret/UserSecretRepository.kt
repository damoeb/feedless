package org.migor.feedless.userSecret

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface UserSecretRepository {

  suspend fun findBySecretKeyValue(
    secretKeyValue: String,
    email: String
  ): UserSecret?

  suspend fun existsByValueAndOwnerId(value: String, ownerId: UserId): Boolean

  suspend fun updateLastUsed(id: UserSecretId, date: LocalDateTime)

  suspend fun findAllByOwnerId(id: UserSecretId): List<UserSecret>
  suspend fun save(userSecret: UserSecret): UserSecret

}
