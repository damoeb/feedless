package org.migor.feedless.user

import java.util.*

data class UserId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}
