package org.migor.feedless.document

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.guard.ResourceGuard
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.document} & ${AppLayer.service}")
class DocumentGuard(
  private val documentRepository: DocumentRepository,
) : ResourceGuard<DocumentId, Document> {

  override suspend fun requireWrite(id: DocumentId): Document = withContext(Dispatchers.IO) {
//    val currentUserId = currentCoroutineContext()[RequestContext.Key]!!.userId!!
//    val currentUser = userRepository.findById(currentUserId)!!
//    if (!(currentUser.admin || currentUser.id == repository.ownerId)) {
//      throw PermissionDeniedException("Must be owner")
//    }
    documentRepository.findById(id) ?: throw NotFoundException("Document $id not found")
  }

  override suspend fun requireRead(id: DocumentId): Document = withContext(Dispatchers.IO) {
    documentRepository.findById(id) ?: throw NotFoundException("Document $id not found")
  }

  override suspend fun requireExecute(id: DocumentId): Document = withContext(Dispatchers.IO) {
    documentRepository.findById(id) ?: throw NotFoundException("Document $id not found")
  }


}
