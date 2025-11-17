package org.migor.feedless.data.jpa.source

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
import org.migor.feedless.actions.WaitAction
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.data.jpa.source.actions.ClickPositionActionEntity
import org.migor.feedless.data.jpa.source.actions.ClickXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.DomActionEntity
import org.migor.feedless.data.jpa.source.actions.ExecuteActionEntity
import org.migor.feedless.data.jpa.source.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.data.jpa.source.actions.ExtractXpathActionEntity
import org.migor.feedless.data.jpa.source.actions.FetchActionEntity
import org.migor.feedless.data.jpa.source.actions.HeaderActionEntity
import org.migor.feedless.data.jpa.source.actions.WaitActionEntity
import org.migor.feedless.source.Source

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface SourceMapper {

  fun toDomain(entity: SourceEntity): Source
  fun toEntity(domain: Source): SourceEntity

  fun toDomain(entity: ClickPositionActionEntity): ClickPositionAction
  fun toEntity(domain: ClickPositionAction): ClickPositionActionEntity

  fun toDomain(entity: ClickXpathActionEntity): ClickXpathAction
  fun toEntity(domain: ClickXpathAction): ClickXpathActionEntity

  fun toDomain(entity: DomActionEntity): DomAction
  fun toEntity(domain: DomAction): DomActionEntity

  fun toDomain(entity: ExecuteActionEntity): ExecuteAction
  fun toEntity(domain: ExecuteAction): ExecuteActionEntity

  fun toDomain(entity: ExtractBoundingBoxActionEntity): ExtractBoundingBoxAction
  fun toEntity(domain: ExtractBoundingBoxAction): ExtractBoundingBoxActionEntity

  fun toDomain(entity: ExtractXpathActionEntity): ExtractXpathAction
  fun toEntity(domain: ExtractXpathAction): ExtractXpathActionEntity

  fun toDomain(entity: FetchActionEntity): FetchAction
  fun toEntity(domain: FetchAction): FetchActionEntity

  fun toDomain(entity: HeaderActionEntity): HeaderAction
  fun toEntity(domain: HeaderAction): HeaderActionEntity

  fun toDomain(entity: WaitActionEntity): WaitAction
  fun toEntity(domain: WaitAction): WaitActionEntity

  companion object {
    val INSTANCE: SourceMapper = Mappers.getMapper(SourceMapper::class.java)
  }
}


fun SourceEntity.toDomain(): Source {
  return SourceMapper.Companion.INSTANCE.toDomain(this)
}

fun Source.toEntity(): SourceEntity {
  return SourceMapper.Companion.INSTANCE.toEntity(this)
}
