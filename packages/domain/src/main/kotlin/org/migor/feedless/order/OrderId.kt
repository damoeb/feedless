package org.migor.feedless.order

import java.util.*

data class OrderId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}
