package org.migor.feedless.document

import java.util.*

data class DocumentId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}
