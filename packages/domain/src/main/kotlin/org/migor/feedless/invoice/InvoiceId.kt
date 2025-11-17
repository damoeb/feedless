package org.migor.feedless.invoice

import java.util.*

data class InvoiceId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


