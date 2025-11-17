package org.migor.feedless.harvest

import java.util.*

data class HarvestId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


