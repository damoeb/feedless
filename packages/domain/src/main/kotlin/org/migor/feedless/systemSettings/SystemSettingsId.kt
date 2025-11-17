package org.migor.feedless.systemSettings

import java.util.*

data class SystemSettingsId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


