package org.migor.feedless.systemSettings

import java.time.LocalDateTime

data class SystemSettings(
  val id: SystemSettingsId = SystemSettingsId(),
  val name: String,
  val valueInt: Int? = null,
  val valueBoolean: Boolean? = null,
  val valueString: String? = null,
  val createdAt: LocalDateTime = LocalDateTime.now()
)

