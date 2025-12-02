package org.migor.feedless.otp

import org.migor.feedless.user.UserId
import org.migor.feedless.util.CryptUtil
import java.time.LocalDateTime

private val otpValidForMinutes: Long = 5
private val otpConfirmCodeLength: Int = 5

data class OneTimePassword(
  val id: OneTimePasswordId = OneTimePasswordId(),
  val password: String = CryptUtil.newCorrId(otpConfirmCodeLength).uppercase(),
  val validUntil: LocalDateTime = LocalDateTime.now().plusMinutes(otpValidForMinutes),
  val userId: UserId,
  val attemptsLeft: Int = 3,
  val createdAt: LocalDateTime = LocalDateTime.now(),
) {
}
