package org.migor.feedless.connector.github

import org.migor.feedless.capability.Capability
import org.migor.feedless.storage.GitAccountCredentials
import org.migor.feedless.storage.GitConnectionCapability
import org.migor.feedless.storage.GitConnectionHandle
import org.migor.feedless.storage.GitRepository

class GithubAccountHandle(gitAccount: GitAccountCredentials) : GitConnectionHandle {
  override fun repositories(): List<GitRepository> {
    TODO("Not yet implemented")
  }

  override fun capability(): Capability<GitConnectionCapability> {
    TODO("Not yet implemented")
  }

}
