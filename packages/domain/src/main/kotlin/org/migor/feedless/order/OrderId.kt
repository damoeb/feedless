package org.migor.feedless.order

import java.util.*

data class OrderId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}
