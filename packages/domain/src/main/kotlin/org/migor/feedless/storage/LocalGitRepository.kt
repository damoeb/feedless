package org.migor.feedless.storage

import org.migor.feedless.capability.CapabilityProvider

interface LocalGitRepository : CapabilityProvider<LocalGitRepositoryCapability> {
  fun checkout(branch: String): LocalGitRepository
  fun files(): List<LocalGitRepositoryFile>
  fun add(files: List<LocalGitRepositoryFile>): LocalGitRepository
  fun commit(): LocalGitRepository
  fun push(): LocalGitRepository
}
