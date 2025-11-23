package org.migor.feedless.userSecret

import java.util.*

data class UserSecretId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))

    constructor() : this(UUID.randomUUID())
}
