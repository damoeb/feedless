package org.migor.feedless.data.jpa.annotation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.annotation.Annotation
import org.migor.feedless.annotation.AnnotationId
import org.migor.feedless.annotation.AnnotationRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.annotation} & ${AppLayer.repository}")
class AnnotationJpaRepository(private val annotationDAO: AnnotationDAO) : AnnotationRepository {

  override fun findById(id: AnnotationId): Annotation? {
    return annotationDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun deleteById(annotationId: AnnotationId) {
    annotationDAO.deleteById(annotationId.uuid)
  }
}

