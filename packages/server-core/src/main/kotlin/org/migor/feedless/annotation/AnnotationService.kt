package org.migor.feedless.annotation

import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.springframework.stereotype.Service

@Service
class AnnotationService {
  suspend fun createAnnotation(corrId: String, data: CreateAnnotationInput): AnnotationEntity {
    TODO()
  }

  suspend fun deleteAnnotation(corrId: String, data: DeleteAnnotationInput): Boolean {
    TODO("Not yet implemented")
  }
}
