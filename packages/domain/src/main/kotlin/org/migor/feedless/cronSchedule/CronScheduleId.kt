package org.migor.feedless.cronSchedule

import java.util.*

data class CronScheduleId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}
