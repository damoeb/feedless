package org.migor.feedless.license

import java.util.*

data class LicenseId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))

    constructor() : this(UUID.randomUUID())
}
