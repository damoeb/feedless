package org.migor.feedless.source

import org.migor.feedless.PageableRequest
import org.migor.feedless.repository.RepositoryId

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

}
