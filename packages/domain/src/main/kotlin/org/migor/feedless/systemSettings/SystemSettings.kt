package org.migor.feedless.systemSettings

import java.time.LocalDateTime

data class SystemSettings(
  val id: SystemSettingsId,
  val name: String,
  val valueInt: Int?,
  val valueBoolean: Boolean?,
  val valueString: String?,
  val createdAt: LocalDateTime
)

