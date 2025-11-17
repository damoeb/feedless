package org.migor.feedless.feature

import java.util.*

data class FeatureGroupId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}


