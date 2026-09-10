package org.migor.feedless.http

import kotlinx.coroutines.currentCoroutineContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryUseCasePort
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/** What an endpoint does to a repository: `GET`s read, every other method writes. */
enum class RepositoryAccess {
  read,
  write,
}

/**
 * The single access rule for `/repositories/{repositoryId}` and everything nested below it.
 *
 * A caller may access a repository when it owns it or is a member of its owning group — the
 * same group assignments `GET /user` reports. A group `viewer` may only read. A public
 * repository may be read by any authenticated caller; writing it still needs the rule above.
 *
 * A denied repository answers exactly like a missing one — the same [NotFoundException] and
 * message, so the API answers 404 and never confirms that a UUID exists.
 */
@Component
@Profile("${AppProfiles.repository} & ${AppProfiles.source} & ${AppProfiles.user} & ${AppLayer.api}")
class RepositoryAccessGuard(
  private val repositoryUseCase: RepositoryUseCasePort,
  private val sourceRepository: SourceRepository,
  private val groupUseCase: GroupUseCasePort,
) {

  suspend fun requireRepository(repositoryId: RepositoryId, access: RepositoryAccess): Repository {
    val userId = currentCoroutineContext()[RequestContext]?.userId ?: throw repositoryNotFound(repositoryId)
    val repository = repositoryUseCase.findById(repositoryId) ?: throw repositoryNotFound(repositoryId)
    if (!mayAccess(repository, userId, access)) {
      throw repositoryNotFound(repositoryId)
    }
    return repository
  }

  /** [requireRepository], then the source — which must belong to that repository. */
  suspend fun requireSource(repositoryId: RepositoryId, sourceId: SourceId, access: RepositoryAccess): Source {
    requireRepository(repositoryId, access)
    val source = sourceRepository.findByIdWithActions(sourceId)
    // Same answer for a missing source and one of another repository.
    if (source == null || source.repositoryId != repositoryId) {
      throw NotFoundException("source ${sourceId.uuid} not found")
    }
    return source
  }

  private suspend fun mayAccess(repository: Repository, userId: UserId, access: RepositoryAccess): Boolean {
    if (access == RepositoryAccess.read && repository.visibility == EntityVisibility.isPublic) {
      return true
    }
    if (repository.ownerId == userId) {
      return true
    }
    val allowedRoles = if (access == RepositoryAccess.read) RoleInGroup.entries else writerRoles
    return groupUseCase.findAllByUserId(userId)
      .any { it.groupId == repository.groupId && it.role in allowedRoles }
  }

  private fun repositoryNotFound(repositoryId: RepositoryId) =
    NotFoundException("repository ${repositoryId.uuid} not found")

  private companion object {
    /** An allow-list, so a role added later is read-only until someone decides otherwise. */
    val writerRoles = setOf(RoleInGroup.owner, RoleInGroup.editor)
  }
}
