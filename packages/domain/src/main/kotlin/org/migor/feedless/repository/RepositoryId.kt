package org.migor.feedless.repository

import java.util.*

data class RepositoryId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
    constructor() : this(UUID.randomUUID())
}
