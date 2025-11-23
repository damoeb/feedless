package org.migor.feedless.user

import java.util.*

data class UserId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
    constructor() : this(UUID.randomUUID())
}
