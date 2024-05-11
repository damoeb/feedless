package org.migor.feedless.annotation

import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.springframework.stereotype.Service

@Service
class AnnotationService {
  fun createAnnotation(corrId: String, data: CreateAnnotationInput): AnnotationEntity {
    TODO()
  }

  fun deleteAnnotation(corrId: String, data: DeleteAnnotationInput): Boolean {
    TODO("Not yet implemented")
  }
}
