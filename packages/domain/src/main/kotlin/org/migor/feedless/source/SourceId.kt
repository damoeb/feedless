package org.migor.feedless.source

import java.util.*

data class SourceId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}

