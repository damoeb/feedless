package org.migor.feedless.annotation

interface AnnotationRepository {
  fun findById(id: AnnotationId): Annotation?
  fun deleteById(annotationId: AnnotationId)
}

