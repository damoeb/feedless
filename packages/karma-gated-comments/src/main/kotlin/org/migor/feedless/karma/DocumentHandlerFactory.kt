package org.migor.feedless.karma

import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.repository.RepositoryRepository
import org.springframework.stereotype.Component

@Component
class DocumentHandlerFactory(
  private val documentRepository: DocumentRepository,
  private val repositoryRepository: RepositoryRepository
) :
  HandlerFactory<DocumentId, DocumentHandle> {
  override fun from(documentId: DocumentId): DocumentHandle {
    return DocumentHandle(documentRepository, repositoryRepository, documentId);
  }

}
