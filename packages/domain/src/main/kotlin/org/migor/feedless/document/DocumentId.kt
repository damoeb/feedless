package org.migor.feedless.document

import java.util.*

data class DocumentId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
    constructor() : this(UUID.randomUUID())
}
