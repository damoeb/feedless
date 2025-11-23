package org.migor.feedless.userGroup

import java.util.*

data class UserGroupAssignmentId(val uuid: UUID) {
    constructor(value: String) : this(UUID.fromString(value))

    constructor() : this(UUID.randomUUID())
}
