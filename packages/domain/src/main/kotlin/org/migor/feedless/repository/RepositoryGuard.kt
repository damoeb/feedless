package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.capability.ShareKeyAccess
import org.migor.feedless.guard.ResourceGuard
import org.migor.feedless.user.User
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.userId
import org.migor.feedless.user.userIdMaybe
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service}")
class RepositoryGuard(
  private val repositoryRepository: RepositoryRepository,
  private val userGuard: UserGuard
) : ResourceGuard<RepositoryId, Repository> {

  override suspend fun requireRead(id: RepositoryId): Repository = withContext(Dispatchers.IO) {
    val (_, repository) = requireRead(coroutineContext.userIdMaybe(), id, coroutineContext[ShareKeyAccess])
    repository
  }

  override suspend fun requireWrite(id: RepositoryId): Repository = withContext(Dispatchers.IO) {
    val (_, repository) = requireRead(coroutineContext.userId(), id)
    require(repository.ownerId == coroutineContext.userId(), { "must be owner" })
    repository
  }

  override suspend fun requireExecute(id: RepositoryId): Repository = withContext(Dispatchers.IO) {
    val (_, repository) = requireRead(coroutineContext.userId(), id)
    repository
  }

  private suspend fun requireRead(
    userId: UserId?,
    id: RepositoryId,
    shareKeyAccess: ShareKeyAccess? = null,
  ): Pair<User?, Repository> {
    val repository = repositoryRepository.findById(id) ?: throw NotFoundException("Repository $id not found")
    if (repository.visibility === EntityVisibility.isPublic || opensWithShareKey(repository, shareKeyAccess)) {
      return Pair(null, repository)
    } else {
      val user =
        userGuard.requireRead(userId ?: throw AccessDeniedException("Repository $id is private, you are not logged in"))
      return Pair(user, repository)
    }
  }

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
