package org.migor.feedless.source

import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositorySourceUpdate

interface SourceUseCasePort {
  suspend fun createSources(sources: List<Source>, repositoryId: RepositoryId): List<Source>
  suspend fun updateSources(repositoryId: RepositoryId, updateInputs: List<RepositorySourceUpdate>)
  suspend fun deleteAllById(repositoryId: RepositoryId, sourceIds: List<SourceId>)
}
