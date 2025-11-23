package org.migor.feedless.data.jpa.report

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.report.Report

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ReportMapper {

    fun toDomain(entity: ReportEntity): Report
    fun toEntity(domain: Report): ReportEntity

    companion object {
        val INSTANCE: ReportMapper = Mappers.getMapper(ReportMapper::class.java)
    }
}
