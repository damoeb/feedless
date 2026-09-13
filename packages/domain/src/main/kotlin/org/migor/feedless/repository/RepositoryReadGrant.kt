package org.migor.feedless.repository

/** Proof that [RepositoryGuard] allowed reading [repositoryId]; only the guard creates one, so a cached read cannot skip the check. */
class RepositoryReadGrant internal constructor(val repositoryId: RepositoryId) {
  override fun toString(): String = "RepositoryReadGrant($repositoryId)"
}
