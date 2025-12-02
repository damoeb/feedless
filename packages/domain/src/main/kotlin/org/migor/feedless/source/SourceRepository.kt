package org.migor.feedless.source

import org.migor.feedless.PageableRequest
import org.migor.feedless.repository.RepositoryId

interface SourceRepository {

  suspend fun setErrorState(
    id: SourceId,
    erroneous: Boolean,
    errorMessage: String? = null
  )

  suspend fun countByRepositoryIdAndLastRecordsRetrieved(repositoryId: RepositoryId, count: Int): Int

  suspend fun findByIdWithActions(sourceId: SourceId): Source?
  suspend fun countByRepositoryId(id: RepositoryId): Long

  suspend fun findAllWithActionsByIdIn(ids: List<SourceId>): List<Source>

  suspend fun findAllByRepositoryIdAndIdIn(repositoryId: RepositoryId, sourceIds: List<SourceId>): List<Source>
  suspend fun save(source: Source): Source
  suspend fun deleteAllById(ids: List<SourceId>)
  suspend fun findById(id: SourceId): Source?
  suspend fun saveAll(sources: List<Source>): List<Source>
  suspend fun findAllByRepositoryIdFiltered(
    repositoryId: RepositoryId,
    pageable: PageableRequest,
    where: SourcesFilter?,
    orders: List<SourceOrderBy>?
  ): List<Source>

}
