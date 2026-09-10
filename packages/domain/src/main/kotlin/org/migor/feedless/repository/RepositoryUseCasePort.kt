package org.migor.feedless.repository

import org.migor.feedless.PageableRequest
import org.migor.feedless.Vertical
import org.migor.feedless.user.UserId

interface RepositoryUseCasePort : RepositoryProvider {
  suspend fun create(commands: List<RepositoryCreate>): List<Repository>
  suspend fun updateRepository(id: RepositoryId, data: RepositoryUpdate)
  suspend fun findAllByUserId(
    pageable: PageableRequest,
    where: RepositoriesFilter?,
    userId: UserId?,
  ): List<Repository>

  suspend fun findById(repositoryId: RepositoryId): Repository?
  suspend fun delete(repositoryId: RepositoryId)
  suspend fun countAll(userId: UserId?, product: Vertical): Int

  /** Same filters (and caller-visibility rules) as [findAllByUserId], without paging. */
  suspend fun countAllByUserId(where: RepositoriesFilter?, userId: UserId?): Int
}
