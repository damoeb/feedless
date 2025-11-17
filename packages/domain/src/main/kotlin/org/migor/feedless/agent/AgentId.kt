package org.migor.feedless.agent

import java.util.*

data class AgentId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


