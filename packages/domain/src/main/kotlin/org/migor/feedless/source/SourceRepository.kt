package org.migor.feedless.source

import org.migor.feedless.PageableRequest
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface SourceRepository {

  fun setErrorState(
    id: SourceId,
    erroneous: Boolean,
    errorMessage: String? = null
  )

  // Atomic updates, not saves of a loaded copy, so overlapping harvests can't lose an error count or overwrite an edit.

  /** The harvest succeeded: resets the error count and message, records [recordsRetrieved]. */
  fun recordHarvestSucceeded(id: SourceId, recordsRetrieved: Int, refreshedAt: LocalDateTime)

  /** The harvest failed: increments the error count in the database, records [errorMessage]. */
  fun recordHarvestFailed(id: SourceId, errorMessage: String?, refreshedAt: LocalDateTime)

  /** A passing failure (rate limit, unreachable host, no items): resets the error count but records [errorMessage]. */
  fun recordHarvestInterrupted(id: SourceId, errorMessage: String?, refreshedAt: LocalDateTime)

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

  /** A query predicate, not a post-filter, so pagination stays correct. Most broken sources first; other users' public repositories excluded. */
  fun findAllForUser(
    userId: UserId,
    groupIds: List<GroupId>,
    pageable: PageableRequest,
    where: SourcesFilter? = null,
  ): List<Source>

}
