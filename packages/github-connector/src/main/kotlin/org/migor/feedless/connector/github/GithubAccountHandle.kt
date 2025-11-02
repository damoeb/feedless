package org.migor.feedless.connector.github

import org.migor.feedless.capability.Capability
import org.migor.feedless.connector.git.GitAccountCredentials
import org.migor.feedless.connector.git.GitConnectionCapability
import org.migor.feedless.connector.git.GitConnectionHandle
import org.migor.feedless.connector.git.GitRepository

class GithubAccountHandle(gitAccount: GitAccountCredentials) : GitConnectionHandle {
  override fun repositories(): List<GitRepository> {
    TODO("Not yet implemented")
  }

  override fun capability(): Capability<GitConnectionCapability> {
    TODO("Not yet implemented")
  }

}
