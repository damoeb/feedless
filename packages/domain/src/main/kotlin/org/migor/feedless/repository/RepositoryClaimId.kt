package org.migor.feedless.repository

import java.util.*

data class RepositoryClaimId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}
