package org.migor.feedless.payment

import java.util.*

data class OrderId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}
