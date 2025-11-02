package org.migor.feedless.storage

interface GitRepository {
  fun clone(): LocalGitRepository
}
