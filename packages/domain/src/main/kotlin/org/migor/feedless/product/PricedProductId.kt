package org.migor.feedless.product

import java.util.*

data class PricedProductId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}
