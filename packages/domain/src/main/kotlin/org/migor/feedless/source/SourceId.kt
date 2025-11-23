package org.migor.feedless.source

import java.util.*

data class SourceId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
    constructor() : this(UUID.randomUUID())
}
