package org.migor.feedless.annotation

import java.util.*

data class AnnotationId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


