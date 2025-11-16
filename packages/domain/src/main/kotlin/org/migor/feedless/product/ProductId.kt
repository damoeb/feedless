package org.migor.feedless.product

import java.util.*

data class ProductId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}
