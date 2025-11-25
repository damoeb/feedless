package org.migor.feedless.data.jpa.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.annotation.TextAnnotation
import org.migor.feedless.annotation.TextAnnotationRepository
import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.annotation} & ${AppLayer.repository}")
class TextAnnotationJpaRepository(private val textAnnotationDAO: TextAnnotationDAO) : TextAnnotationRepository {

    override suspend fun existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
        fromChar: Int,
        toChar: Int,
        userId: UserId,
        documentId: DocumentId?,
        repositoryId: RepositoryId?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            textAnnotationDAO.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
                fromChar,
                toChar,
                userId.uuid,
                documentId?.uuid,
                repositoryId?.uuid
            )
        }
    }

    override suspend fun save(textAnnotation: TextAnnotation): TextAnnotation {
        return withContext(Dispatchers.IO) {
            textAnnotationDAO.save(textAnnotation.toEntity()).toDomain()
        }
    }
}

