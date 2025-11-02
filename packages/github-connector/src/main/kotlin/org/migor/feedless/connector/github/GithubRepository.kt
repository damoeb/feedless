package org.migor.feedless.connector.github

import org.migor.feedless.storage.GitRepository
import org.migor.feedless.storage.LocalGitRepository
import java.net.URI

class GithubRepository(uri: URI) : GitRepository {
  override fun clone(): LocalGitRepository {
    TODO("Not yet implemented")
  }

}
