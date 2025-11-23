package org.migor.feedless.data.jpa.systemSettings

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.systemSettings.SystemSettings

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface SystemSettingsMapper {

    fun toDomain(entity: SystemSettingsEntity): SystemSettings
    fun toEntity(domain: SystemSettings): SystemSettingsEntity

    companion object {
        val INSTANCE: SystemSettingsMapper = Mappers.getMapper(SystemSettingsMapper::class.java)
    }
}
