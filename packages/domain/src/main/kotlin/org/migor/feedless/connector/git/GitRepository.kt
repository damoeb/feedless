package org.migor.feedless.connector.git

interface GitRepository {
  fun clone(): LocalGitRepository
}
