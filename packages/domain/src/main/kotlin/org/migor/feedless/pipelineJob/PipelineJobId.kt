package org.migor.feedless.pipelineJob

import java.util.*

data class PipelineJobId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))

  constructor() : this(UUID.randomUUID())
}


