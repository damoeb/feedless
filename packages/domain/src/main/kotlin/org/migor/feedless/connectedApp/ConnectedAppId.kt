package org.migor.feedless.connectedApp

import java.util.*

data class ConnectedAppId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}


