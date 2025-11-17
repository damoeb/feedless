package org.migor.feedless.data.jpa.connectedApp

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.connectedApp.ConnectedApp

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ConnectedAppMapper {

  fun toDomain(entity: ConnectedAppEntity): ConnectedApp
  fun toEntity(domain: ConnectedApp): ConnectedAppEntity

  companion object {
    val INSTANCE: ConnectedAppMapper = Mappers.getMapper(ConnectedAppMapper::class.java)
  }
}


