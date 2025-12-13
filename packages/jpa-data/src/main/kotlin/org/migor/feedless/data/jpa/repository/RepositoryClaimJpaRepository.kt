package org.migor.feedless.data.jpa.repository

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.repositoryClaim.RepositoryClaimDAO
import org.migor.feedless.data.jpa.repositoryClaim.toDomain
import org.migor.feedless.data.jpa.repositoryClaim.toEntity
import org.migor.feedless.repository.RepositoryClaim
import org.migor.feedless.repository.RepositoryClaimId
import org.migor.feedless.repository.RepositoryClaimRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
class RepositoryClaimJpaRepository(private val repositoryClaimDAO: RepositoryClaimDAO) : RepositoryClaimRepository {

  override fun findById(id: RepositoryClaimId): RepositoryClaim? {
    return repositoryClaimDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun deleteById(id: RepositoryClaimId) {
    repositoryClaimDAO.deleteById(id.uuid)
  }

  override fun save(repositoryClaim: RepositoryClaim): RepositoryClaim {
    return repositoryClaimDAO.save(repositoryClaim.toEntity()).toDomain()
  }
}
