package org.migor.feedless.repository

interface RepositoryClaimRepository {

  suspend fun findById(repositoryClaim: RepositoryClaimId): RepositoryClaim?
  suspend fun save(repositoryClaim: RepositoryClaim): RepositoryClaim

  suspend fun deleteById(id: RepositoryClaimId)
}
