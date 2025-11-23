package org.migor.feedless.data.jpa.oneTimePassword

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.otp.OneTimePassword

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface OneTimePasswordMapper {

    fun toDomain(entity: OneTimePasswordEntity): OneTimePassword
    fun toEntity(domain: OneTimePassword): OneTimePasswordEntity

    companion object {
        val INSTANCE: OneTimePasswordMapper = Mappers.getMapper(OneTimePasswordMapper::class.java)
    }
}
