package org.migor.feedless.feature

import java.util.*

data class FeatureId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


