package org.migor.feedless.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.user.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class PermissionService(
  private val userDAO: UserDAO,
  private val repositoryDAO: RepositoryDAO
) {
  private val log = LoggerFactory.getLogger(PermissionService::class.simpleName)

//  @Autowired
//  private lateinit var userGroupAssignmentDAO: UserGroupAssignmentDAO

//  @Autowired
//  private lateinit var documentDAO: DocumentDAO

  suspend fun canWrite(document: DocumentEntity) {
    withContext(Dispatchers.IO) {
      val repository = repositoryDAO.findById(document.repositoryId).orElseThrow()
      canWrite(repository)
    }
  }

  suspend fun canWrite(repository: RepositoryEntity) {
    val currentUserId = currentCoroutineContext()[RequestContext]!!.userId!!
    withContext(Dispatchers.IO) {
      val currentUser = userDAO.findById(currentUserId.value).orElseThrow()
      if (!(currentUser.admin || currentUser.id == repository.ownerId)) {
        throw PermissionDeniedException("Must be owner")
      }
    }
  }

  fun canReadDocument(documentId: DocumentId) {

  }
}
