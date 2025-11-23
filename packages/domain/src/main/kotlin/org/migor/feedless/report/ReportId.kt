package org.migor.feedless.report

import java.util.*

data class ReportId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
    constructor() : this(UUID.randomUUID())
}
