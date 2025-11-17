package org.migor.feedless.actions

import java.util.*

data class ScrapeActionId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


