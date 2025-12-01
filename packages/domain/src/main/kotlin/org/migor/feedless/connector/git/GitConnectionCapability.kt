package org.migor.feedless.connector.git

import kotlinx.serialization.Serializable

@Serializable
data class GitConnectionCapability(val connectionConfig: GitConnectionConfig)
