package org.migor.feedless.oneTimePassword

import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface OneTimePasswordRepository {
  fun deleteAllByValidUntilBefore(now: LocalDateTime)
  fun findFirstByUserIdOrderByCreatedAtDesc(userId: UserId): OneTimePassword?
  fun findById(id: OneTimePasswordId): OneTimePassword?
  fun save(otp: OneTimePassword): OneTimePassword
  fun deleteById(id: OneTimePasswordId)
}
