package org.migor.feedless.service

import org.migor.feedless.data.jpa.models.UserSecretEntity
import java.util.*

interface UserSecretService {
  fun findBySecretKeyValue(secretKeyValue: String, email: String): Optional<UserSecretEntity>
  fun updateLastUsed(id: UUID, date: Date)

}
