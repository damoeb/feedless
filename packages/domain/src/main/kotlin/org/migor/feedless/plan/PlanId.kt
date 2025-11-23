package org.migor.feedless.plan

import java.util.*

data class PlanId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))

    constructor() : this(UUID.randomUUID())
}
