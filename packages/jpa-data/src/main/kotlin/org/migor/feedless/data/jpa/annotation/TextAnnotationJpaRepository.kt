package org.migor.feedless.data.jpa.annotation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.annotation.TextAnnotation
import org.migor.feedless.annotation.TextAnnotationRepository
import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.annotation} & ${AppLayer.repository}")
class TextAnnotationJpaRepository(private val textAnnotationDAO: TextAnnotationDAO) : TextAnnotationRepository {

  override fun existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
    fromChar: Int,
    toChar: Int,
    userId: UserId,
    documentId: DocumentId?,
    repositoryId: RepositoryId?
  ): Boolean {
    return textAnnotationDAO.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
      fromChar,
      toChar,
      userId.uuid,
      documentId?.uuid,
      repositoryId?.uuid
    )
  }

  override fun save(textAnnotation: TextAnnotation): TextAnnotation {
    return textAnnotationDAO.save(textAnnotation.toEntity()).toDomain()
  }
}

