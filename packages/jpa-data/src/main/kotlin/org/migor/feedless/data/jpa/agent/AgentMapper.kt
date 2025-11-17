package org.migor.feedless.data.jpa.agent

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.agent.Agent

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface AgentMapper {

  fun toDomain(entity: AgentEntity): Agent
  fun toEntity(domain: Agent): AgentEntity

  companion object {
    val INSTANCE: AgentMapper = Mappers.getMapper(AgentMapper::class.java)
  }
}

