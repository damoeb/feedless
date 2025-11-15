package org.migor.feedless.license

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.jpa.license.LicenseEntity

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface LicenseMapper {

  fun toDomain(entity: LicenseEntity): License
  fun toEntity(domain: License): LicenseEntity

  companion object {
    val INSTANCE: LicenseMapper = Mappers.getMapper(LicenseMapper::class.java)
  }
}
