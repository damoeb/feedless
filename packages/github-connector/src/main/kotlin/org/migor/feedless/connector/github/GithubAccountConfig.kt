package org.migor.feedless.connector.github

import kotlinx.serialization.Serializable
import org.migor.feedless.connector.git.GitConnectionConfig

@Serializable
data class GithubAccountConfig(val capability: GithubCapability) : GitConnectionConfig
