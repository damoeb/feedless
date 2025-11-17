package org.migor.feedless.product

import java.util.*

data class PricedProductId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}
