package org.migor.feedless.report

import java.time.LocalDateTime
import java.util.*

data class ReportRecipientId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}

/** One row per address, so opt-in applies to every report sent there, not to one report. */
data class ReportRecipient(
  val id: ReportRecipientId = ReportRecipientId(),
  val email: String,
  val optInRequired: Boolean = false,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** Plus-tags and provider dots stay significant; only case and surrounding spaces do not. */
fun normalizeEmail(email: String): String = email.trim().lowercase()
