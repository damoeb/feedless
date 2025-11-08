package org.migor.feedless.connector.github

import org.migor.feedless.connector.git.GitConnectionConfig

data class GithubAccountConfig(val capability: GithubCapability) : GitConnectionConfig
