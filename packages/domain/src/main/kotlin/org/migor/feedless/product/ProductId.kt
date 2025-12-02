package org.migor.feedless.product

import java.util.*

data class ProductId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}
