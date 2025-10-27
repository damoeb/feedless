package org.migor.feedless.attachment

import java.util.*

data class AttachmentId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}
