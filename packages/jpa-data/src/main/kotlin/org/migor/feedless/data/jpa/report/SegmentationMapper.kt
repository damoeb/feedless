package org.migor.feedless.data.jpa.report

import org.locationtech.jts.geom.Point
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.report.Segmentation

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface SegmentationMapper {

    fun toDomain(entity: SegmentationEntity): Segmentation
    fun toEntity(domain: Segmentation): SegmentationEntity

    fun toDomain(value: LatLonPoint?): Point? = value?.let { JtsUtil.createPoint(value.latitude, value.longitude) }
    fun toEntity(value: Point?): LatLonPoint? = value?.let { LatLonPoint(value.x, value.y) }

    companion object {
        val INSTANCE: SegmentationMapper = Mappers.getMapper(SegmentationMapper::class.java)
    }
}
