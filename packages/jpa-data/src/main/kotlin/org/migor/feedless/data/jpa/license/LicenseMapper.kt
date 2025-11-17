package org.migor.feedless.data.jpa.license

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.license.License

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface LicenseMapper {

  fun toDomain(entity: LicenseEntity): License
  fun toEntity(domain: License): LicenseEntity

  companion object {
    val INSTANCE: LicenseMapper = Mappers.getMapper(LicenseMapper::class.java)
  }
}


