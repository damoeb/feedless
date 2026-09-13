package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.capability.ShareKeyAccess
import org.migor.feedless.guard.ResourceGuard
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.userId
import org.migor.feedless.user.userIdMaybe
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class RepositoryGuard(
  private val repositoryRepository: RepositoryRepository,
  private val userGuard: UserGuard,
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
) : ResourceGuard<RepositoryId, Repository> {

  /** Public, a valid share key, or a logged-in owner or member of the owning group; everyone else sees a missing repository. */
  override suspend fun requireRead(id: RepositoryId): Repository = withContext(Dispatchers.IO) {
    val repository = repositoryRepository.findById(id) ?: throw notFound(id)
    if (repository.visibility === EntityVisibility.isPublic ||
      opensWithShareKey(repository, coroutineContext[ShareKeyAccess])
    ) {
      return@withContext repository
    }
    // same answer as a missing repository, so a private id is never confirmed
    val userId = coroutineContext.userIdMaybe() ?: throw notFound(id)
    if (!isActiveOwnerOrMember(repository, userId)) {
      throw notFound(id)
    }
    repository
  }

  suspend fun requireReadGrant(id: RepositoryId): RepositoryReadGrant {
    requireRead(id)
    return RepositoryReadGrant(id)
  }

  /** Sources and other configuration: owner or group member only, even on a public repository; a share key does not count. */
  suspend fun mayReadConfiguration(repository: Repository): Boolean {
    val userId = currentCoroutineContext().userIdMaybe() ?: return false
    return try {
      isActiveOwnerOrMember(repository, userId)
    } catch (e: IllegalArgumentException) {
      // a banned owner or member sees no configuration
      false
    }
  }

  override suspend fun requireWrite(id: RepositoryId): Repository = withContext(Dispatchers.IO) {
    val repository = requireLoggedInRead(coroutineContext.userId(), id)
    require(repository.ownerId == coroutineContext.userId(), { "must be owner" })
    repository
  }

  override suspend fun requireExecute(id: RepositoryId): Repository = withContext(Dispatchers.IO) {
    requireLoggedInRead(coroutineContext.userId(), id)
  }

  // the write and execute rules predate the owner-or-member read rule and stay unchanged
  private suspend fun requireLoggedInRead(userId: UserId?, id: RepositoryId): Repository {
    val repository = repositoryRepository.findById(id) ?: throw notFound(id)
    if (repository.visibility !== EntityVisibility.isPublic) {
      userGuard.requireRead(userId ?: throw AccessDeniedException("Repository $id is private, you are not logged in"))
    }
    return repository
  }

  private suspend fun isActiveOwnerOrMember(repository: Repository, userId: UserId): Boolean =
    RepositoryAccessRule.isActiveOwnerOrMember(repository, userId, userGuard) {
      withContext(Dispatchers.IO) { userGroupAssignmentRepository.findAllByUserId(userId) }
    }

  private fun notFound(id: RepositoryId) = NotFoundException("Repository $id not found")

  private fun opensWithShareKey(repository: Repository, access: ShareKeyAccess?): Boolean {
    if (access == null || access.repositoryId != repository.id) {
      return false
    }
    if (access.shareKey.isBlank() || repository.shareKey.isBlank()) {
      return false
    }
    // constant time, so response timing does not leak how much of the key matched
    return MessageDigest.isEqual(access.shareKey.toByteArray(), repository.shareKey.toByteArray())
  }
}
