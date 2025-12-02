package org.migor.feedless.data.jpa.source

import org.locationtech.jts.geom.Point
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.actions.ClickXpathAction
import org.migor.feedless.actions.DomAction
import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.HeaderAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.actions.WaitAction
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.data.jpa.source.actions.ClickPositionActionEntity
import org.migor.feedless.data.jpa.source.actions.ClickPositionActionMapper
import org.migor.feedless.data.jpa.source.actions.ClickXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.ClickXpathActionMapper
import org.migor.feedless.data.jpa.source.actions.DomActionEntity
import org.migor.feedless.data.jpa.source.actions.DomActionMapper
import org.migor.feedless.data.jpa.source.actions.ExecuteActionEntity
import org.migor.feedless.data.jpa.source.actions.ExecuteActionMapper
import org.migor.feedless.data.jpa.source.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.data.jpa.source.actions.ExtractBoundingBoxActionMapper
import org.migor.feedless.data.jpa.source.actions.ExtractXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.ExtractXpathActionMapper
import org.migor.feedless.data.jpa.source.actions.FetchActionEntity
import org.migor.feedless.data.jpa.source.actions.FetchActionMapper
import org.migor.feedless.data.jpa.source.actions.HeaderActionEntity
import org.migor.feedless.data.jpa.source.actions.HeaderActionMapper
import org.migor.feedless.data.jpa.source.actions.ScrapeActionEntity
import org.migor.feedless.data.jpa.source.actions.WaitActionEntity
import org.migor.feedless.data.jpa.source.actions.WaitActionMapper
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.source.Source

@Mapper(
  unmappedTargetPolicy = ReportingPolicy.IGNORE,
  uses = [
    IdMappers::class,
    ClickPositionActionMapper::class,
    ClickXpathActionMapper::class,
    DomActionMapper::class,
    ExecuteActionMapper::class,
    ExtractBoundingBoxActionMapper::class,
    ExtractXpathActionMapper::class,
    FetchActionMapper::class,
    HeaderActionMapper::class,
    WaitActionMapper::class
  ]
)
interface SourceMapper {

  fun toDomain(entity: SourceEntity): Source
  fun toEntity(domain: Source): SourceEntity

  fun mapActions(entities: List<ScrapeActionEntity>): List<ScrapeAction> {
    return entities.map { it.toDomain() }
  }

  fun mapActionsToEntity(actions: List<ScrapeAction>): List<ScrapeActionEntity> {
    return actions.map { it.toEntity() }
  }

  // Convert JTS Point (entity) to LatLonPoint (domain)
  fun pointToLatLon(value: Point?): LatLonPoint? = value?.let { LatLonPoint(it.x, it.y) }

  // Convert LatLonPoint (domain) to JTS Point (entity)
  fun latLonToPoint(value: LatLonPoint?): Point? = value?.let { JtsUtil.createPoint(it.latitude, it.longitude) }

  companion object {
    val INSTANCE: SourceMapper = Mappers.getMapper(SourceMapper::class.java)
  }
}


fun SourceEntity.toDomain(): Source {
  return SourceMapper.INSTANCE.toDomain(this)
}

fun Source.toEntity(): SourceEntity {
  return SourceMapper.INSTANCE.toEntity(this)
}

fun ScrapeActionEntity.toDomain(): ScrapeAction {
  return when (this) {
    is ClickPositionActionEntity -> ClickPositionActionMapper.INSTANCE.toDomain(this)
    is ClickXpathActionEntity -> ClickXpathActionMapper.INSTANCE.toDomain(this)
    is DomActionEntity -> DomActionMapper.INSTANCE.toDomain(this)
    is ExecuteActionEntity -> ExecuteActionMapper.INSTANCE.toDomain(this)
    is ExtractBoundingBoxActionEntity -> ExtractBoundingBoxActionMapper.INSTANCE.toDomain(this)
    is ExtractXpathActionEntity -> ExtractXpathActionMapper.INSTANCE.toDomain(this)
    is FetchActionEntity -> FetchActionMapper.INSTANCE.toDomain(this)
    is HeaderActionEntity -> HeaderActionMapper.INSTANCE.toDomain(this)
    is WaitActionEntity -> WaitActionMapper.INSTANCE.toDomain(this)
    else -> throw IllegalArgumentException("Unknown ScrapeActionEntity type: ${this.javaClass}")
  }
}

fun ScrapeAction.toEntity(): ScrapeActionEntity {
  return when (this) {
    is ClickPositionAction -> ClickPositionActionMapper.INSTANCE.toEntity(this)
    is ClickXpathAction -> ClickXpathActionMapper.INSTANCE.toEntity(this)
    is DomAction -> DomActionMapper.INSTANCE.toEntity(this)
    is ExecuteAction -> ExecuteActionMapper.INSTANCE.toEntity(this)
    is ExtractBoundingBoxAction -> ExtractBoundingBoxActionMapper.INSTANCE.toEntity(this)
    is ExtractXpathAction -> ExtractXpathActionMapper.INSTANCE.toEntity(this)
    is FetchAction -> FetchActionMapper.INSTANCE.toEntity(this)
    is HeaderAction -> HeaderActionMapper.INSTANCE.toEntity(this)
    is WaitAction -> WaitActionMapper.INSTANCE.toEntity(this)
  }
}
