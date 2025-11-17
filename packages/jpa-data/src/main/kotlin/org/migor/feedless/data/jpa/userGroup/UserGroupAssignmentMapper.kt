package org.migor.feedless.data.jpa.userGroup

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.userGroup.UserGroupAssignment

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface UserGroupAssignmentMapper {

  fun toDomain(entity: UserGroupAssignmentEntity): UserGroupAssignment
  fun toEntity(domain: UserGroupAssignment): UserGroupAssignmentEntity

  companion object {
    val INSTANCE: UserGroupAssignmentMapper = Mappers.getMapper(UserGroupAssignmentMapper::class.java)
  }
}


