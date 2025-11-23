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
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class PermissionService(
    private val userDAO: UserRepository,
    private val repositoryDAO: RepositoryRepository
) {
    private val log = LoggerFactory.getLogger(PermissionService::class.simpleName)

//  @Autowired
//  private lateinit var userGroupAssignmentDAO: UserGroupAssignmentDAO

//  @Autowired
//  private lateinit var documentDAO: DocumentDAO

    suspend fun canWrite(document: Document) {
        val repository = repositoryDAO.findById(document.repositoryId)!!
        canWrite(repository)
    }

    suspend fun canWrite(repository: Repository) {
        val currentUserId = currentCoroutineContext()[RequestContext]!!.userId!!
        val currentUser = userDAO.findById(currentUserId)!!
        if (!(currentUser.admin || currentUser.id == repository.ownerId)) {
            throw PermissionDeniedException("Must be owner")
        }
    }

    fun canReadDocument(documentId: DocumentId) {

    }
}
