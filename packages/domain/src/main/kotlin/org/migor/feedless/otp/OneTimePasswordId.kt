package org.migor.feedless.otp

import java.util.*

data class OneTimePasswordId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))

    constructor() : this(UUID.randomUUID())
}
