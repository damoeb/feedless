package org.migor.feedless.oneTimePassword

import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface OneTimePasswordRepository {
  suspend fun deleteAllByValidUntilBefore(now: LocalDateTime)
  suspend fun findFirstByUserIdOrderByCreatedAtDesc(userId: UserId): OneTimePassword?
  suspend fun findById(id: OneTimePasswordId): OneTimePassword?
  suspend fun save(otp: OneTimePassword): OneTimePassword
  suspend fun deleteById(id: OneTimePasswordId)
}
