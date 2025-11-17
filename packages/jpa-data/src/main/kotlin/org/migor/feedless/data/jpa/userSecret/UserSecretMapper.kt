package org.migor.feedless.data.jpa.userSecret

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.userSecret.UserSecret

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface UserSecretMapper {

  fun toDomain(entity: UserSecretEntity): UserSecret
  fun toEntity(domain: UserSecret): UserSecretEntity

  companion object {
    val INSTANCE: UserSecretMapper = Mappers.getMapper(UserSecretMapper::class.java)
  }
}


