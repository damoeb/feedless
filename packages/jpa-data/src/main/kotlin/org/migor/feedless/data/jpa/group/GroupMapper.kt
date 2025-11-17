package org.migor.feedless.data.jpa.group

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.group.Group

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface GroupMapper {

  fun toDomain(entity: GroupEntity): Group
  fun toEntity(domain: Group): GroupEntity

  companion object {
    val INSTANCE: GroupMapper = Mappers.getMapper(GroupMapper::class.java)
  }
}

