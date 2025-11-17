package org.migor.feedless.data.jpa.repository

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.repository.Repository

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface RepositoryMapper {

  fun toDomain(entity: RepositoryEntity): Repository
  fun toEntity(domain: Repository): RepositoryEntity

  companion object {
    val INSTANCE: RepositoryMapper = Mappers.getMapper(RepositoryMapper::class.java)
  }
}

