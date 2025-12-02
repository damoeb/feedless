package org.migor.feedless.otp

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class OneTimePassword(
  val id: OneTimePasswordId = OneTimePasswordId(),
  val password: String,
  val validUntil: LocalDateTime,
  val userId: UserId,
  val attemptsLeft: Int,
  val createdAt: LocalDateTime = LocalDateTime.now(),
) {
}
