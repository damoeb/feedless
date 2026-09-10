package org.migor.feedless.source

import org.migor.feedless.PageableRequest
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId

interface SourceRepository {

  fun setErrorState(
    id: SourceId,
    erroneous: Boolean,
    errorMessage: String? = null
  )

  fun countSourcesWithProblems(repositoryId: RepositoryId): Int

  fun findByIdWithActions(sourceId: SourceId): Source?
  fun countByRepositoryId(id: RepositoryId): Long

  fun findAllWithActionsByIdIn(ids: List<SourceId>): List<Source>

  fun findAllByRepositoryIdAndIdIn(repositoryId: RepositoryId, sourceIds: List<SourceId>): List<Source>
  fun save(source: Source): Source
  fun deleteAllById(ids: List<SourceId>)
  fun findById(id: SourceId): Source?
  fun saveAll(sources: List<Source>): List<Source>
  fun findAllByRepositoryIdFiltered(
    repositoryId: RepositoryId,
    pageable: PageableRequest,
    where: SourcesFilter? = null,
    orders: List<SourceOrderBy>? = null
  ): List<Source>

  /**
   * Sources across every repository [userId] owns or belongs to via its owning group (any role) —
   * the same per-repository access rule http-api's RepositoryAccessGuard applies, expressed here
   * as a query predicate so pagination stays correct. Public repositories of other users are not
   * included. Ordered by errorsInSuccession desc, then lastRefreshedAt desc — the most broken
   * sources come first.
   */
  fun findAllForUser(
    userId: UserId,
    groupIds: List<GroupId>,
    pageable: PageableRequest,
    where: SourcesFilter? = null,
  ): List<Source>

}
