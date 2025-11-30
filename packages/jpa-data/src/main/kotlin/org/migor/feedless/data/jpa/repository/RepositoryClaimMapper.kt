package org.migor.feedless.data.jpa.repository

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.data.jpa.repositoryClaim.RepositoryClaimEntity
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryClaim

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface RepositoryClaimMapper {

  fun toDomain(entity: RepositoryClaimEntity): RepositoryClaim
  fun toEntity(domain: RepositoryClaim): RepositoryClaimEntity

  companion object {
    val INSTANCE: RepositoryClaimMapper = Mappers.getMapper(RepositoryClaimMapper::class.java)
  }
}

