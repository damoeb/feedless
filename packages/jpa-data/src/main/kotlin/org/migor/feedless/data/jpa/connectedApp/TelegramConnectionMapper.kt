package org.migor.feedless.data.jpa.connectedApp

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.connectedApp.TelegramConnection

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface TelegramConnectionMapper {

  fun toDomain(entity: TelegramConnectionEntity): TelegramConnection
  fun toEntity(domain: TelegramConnection): TelegramConnectionEntity

  companion object {
    val INSTANCE: TelegramConnectionMapper = Mappers.getMapper(TelegramConnectionMapper::class.java)
  }
}


