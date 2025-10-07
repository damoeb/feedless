package org.migor.feedless.otp

import java.time.LocalDateTime
import java.util.*

data class OneTimePassword (
  val id: UUID,
  val password: String,
  val validUntil: LocalDateTime,
  val userId: UUID
)
