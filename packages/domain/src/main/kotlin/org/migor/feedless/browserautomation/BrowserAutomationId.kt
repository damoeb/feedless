package org.migor.feedless.browserautomation

import java.util.*

data class BrowserAutomationId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}



