package org.migor.feedless.data.jpa.oneTimePassword

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile(AppLayer.repository)
class OneTimePasswordJpaRepository(private val oneTimePasswordDAO: OneTimePasswordDAO) : OneTimePasswordRepository {
  override suspend fun deleteAllByValidUntilBefore(now: LocalDateTime) {
    withContext(Dispatchers.IO) {
      oneTimePasswordDAO.deleteAllByValidUntilBefore(now)
    }
  }

  override suspend fun findFirstByUserIdOrderByCreatedAtDesc(userId: UserId): OneTimePassword? {
    return withContext(Dispatchers.IO) {
      oneTimePasswordDAO.findFirstByUserIdOrderByCreatedAtDesc(userId.uuid)?.toDomain()
    }
  }

  override suspend fun findById(id: OneTimePasswordId): OneTimePassword? {
    return withContext(Dispatchers.IO) {
      oneTimePasswordDAO.findById(id.uuid).getOrNull()?.toDomain()
    }
  }

  override suspend fun save(otp: OneTimePassword): OneTimePassword {
    return withContext(Dispatchers.IO) {
      oneTimePasswordDAO.save(otp.toEntity()).toDomain()
    }
  }

  override suspend fun deleteById(id: OneTimePasswordId) {
    withContext(Dispatchers.IO) {
      oneTimePasswordDAO.deleteById(id.uuid)
    }
  }
}
