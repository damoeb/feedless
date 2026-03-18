package org.migor.feedless.karma

import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.UserId

class DocumentHandle(
  private val documentRepository: DocumentRepository,
  private val repositoryRepository: RepositoryRepository,
  private val documentId: DocumentId
) {
  fun authorId(): UserId {
    val document = documentRepository.findById(documentId)
      ?: throw IllegalArgumentException("No document with id $documentId")
    val repository = repositoryRepository.findById(document.repositoryId)
      ?: throw IllegalArgumentException("No document with id $documentId")
    return repository.ownerId
  }

  fun updateScore() {
    // todo
  }
}
