package org.migor.feedless.userSecret

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface UserSecretRepository {

  fun findBySecretKeyValue(
    secretKeyValue: String,
    email: String
  ): UserSecret?

  fun existsByValueAndOwnerId(value: String, ownerId: UserId): Boolean

  fun updateLastUsed(id: UserSecretId, date: LocalDateTime)

  fun findAllByOwnerId(id: UserId): List<UserSecret>
  fun save(userSecret: UserSecret): UserSecret
  fun findById(userSecretId: UserSecretId): UserSecret?
  fun deleteById(id: UserSecretId)

}
