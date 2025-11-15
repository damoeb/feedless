package org.migor.feedless.user

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.jpa.user.UserEntity

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface UserMapper {

  fun toDomain(entity: UserEntity): User
  fun toEntity(domain: User): UserEntity

  companion object {
    val INSTANCE: UserMapper = Mappers.getMapper(UserMapper::class.java)
  }
}
