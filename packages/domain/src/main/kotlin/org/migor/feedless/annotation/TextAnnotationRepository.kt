package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId

interface TextAnnotationRepository {

  fun existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
    fromChar: Int,
    toChar: Int,
    userId: UserId,
    documentId: DocumentId?,
    repositoryId: RepositoryId?
  ): Boolean

  fun save(textAnnotation: TextAnnotation): TextAnnotation
}

