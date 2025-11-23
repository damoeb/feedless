package org.migor.feedless.group

import java.util.*

data class GroupId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
    constructor() : this(UUID.randomUUID())
}
