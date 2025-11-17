package org.migor.feedless.report

import java.util.*

data class ReportId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}


