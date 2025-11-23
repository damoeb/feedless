package org.migor.feedless.connectedApp

import java.util.*

data class ConnectedAppId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
    constructor() : this(UUID.randomUUID())
}
