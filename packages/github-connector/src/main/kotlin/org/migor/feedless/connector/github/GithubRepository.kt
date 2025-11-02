package org.migor.feedless.connector.github

import org.migor.feedless.connector.git.GitRepository
import org.migor.feedless.connector.git.LocalGitRepository
import java.net.URI

class GithubRepository(uri: URI) : GitRepository {
  override fun clone(): LocalGitRepository {
    TODO("Not yet implemented")
  }

}
