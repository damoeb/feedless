package org.migor.feedless.annotation

interface AnnotationRepository {
    suspend fun findById(id: AnnotationId): Annotation?
    suspend fun delete(annotation: Annotation)
}

