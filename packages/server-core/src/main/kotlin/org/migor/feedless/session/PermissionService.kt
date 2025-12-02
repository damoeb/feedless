package org.migor.feedless.session

import kotlinx.coroutines.currentCoroutineContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


@Service
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class PermissionService(
  private val userRepository: UserRepository,
  private val repositoryRepository: RepositoryRepository
) {

  suspend fun canWrite(document: Document) {
    val repository = repositoryRepository.findById(document.repositoryId)!!
    canWrite(repository)
  }

  suspend fun canWrite(repository: Repository) {
    val currentUserId = currentCoroutineContext()[RequestContext]!!.userId!!
    val currentUser = userRepository.findById(currentUserId)!!
    if (!(currentUser.admin || currentUser.id == repository.ownerId)) {
      throw PermissionDeniedException("Must be owner")
    }
  }

  fun canReadDocument(documentId: DocumentId) {

  }
}
