package org.migor.feedless.data.jpa.annotation

//@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class, VoteMapper::class])
//interface AnnotationMapper {
//
//  @SubclassMapping(source = VoteEntity::class, target = Vote::class)
//  fun toDomain(entity: AnnotationEntity): Annotation
//
//  @SubclassMapping(source = Vote::class, target = VoteEntity::class)
//  fun toEntity(domain: Annotation): AnnotationEntity
//
//  companion object {
//    val INSTANCE: AnnotationMapper = Mappers.getMapper(AnnotationMapper::class.java)
//  }
//}


