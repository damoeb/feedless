package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId

interface TextAnnotationRepository {

    suspend fun existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
        fromChar: Int,
        toChar: Int,
        userId: UserId,
        documentId: DocumentId?,
        repositoryId: RepositoryId?
    ): Boolean

    suspend fun save(textAnnotation: TextAnnotation): TextAnnotation
}

