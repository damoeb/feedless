package org.migor.feedless.connector.github

import org.migor.feedless.capability.Capability
import org.migor.feedless.storage.AnonymousGitConnectionConfig
import org.migor.feedless.storage.GitConnectionCapability
import org.migor.feedless.storage.GitConnectionConfig
import org.migor.feedless.storage.GitConnectionHandle
import org.migor.feedless.storage.GitConnector
import org.migor.feedless.storage.GitRepository
import org.springframework.stereotype.Service

@Service
class GithubConnector : GitConnector {
  override fun connect(connectionConfig: GitConnectionConfig): GitConnectionHandle {
    if (connectionConfig is AnonymousGitConnectionConfig) {
      val capability = GitConnectionCapability(connectionConfig)
      return object : GitConnectionHandle {
        override fun repositories(): List<GitRepository> {
          return connectionConfig.urls.map { GithubRepository(it) }
        }

        override fun capability(): Capability<GitConnectionCapability> {
          return Capability("", capability)
        }
      }
    }
    TODO("Not yet implemented")
  }
}
