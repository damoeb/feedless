package org.migor.feedless.data.jpa.user

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.user.User

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface UserMapper {

  fun toDomain(entity: UserEntity): User
  fun toEntity(domain: User): UserEntity

  companion object {
    val INSTANCE: UserMapper = Mappers.getMapper(UserMapper::class.java)
  }
}
