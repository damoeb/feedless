package org.migor.feedless.repository

interface RepositoryClaimRepository {

  fun findById(id: RepositoryClaimId): RepositoryClaim?
  fun save(repositoryClaim: RepositoryClaim): RepositoryClaim

  fun deleteById(id: RepositoryClaimId)
}
