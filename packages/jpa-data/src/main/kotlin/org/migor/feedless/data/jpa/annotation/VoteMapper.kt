package org.migor.feedless.data.jpa.annotation

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.annotation.Vote

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface VoteMapper {

  fun toDomain(entity: VoteEntity): Vote
  fun toEntity(domain: Vote): VoteEntity

  companion object {
    val INSTANCE: VoteMapper = Mappers.getMapper(VoteMapper::class.java)
  }
}


