package org.migor.feedless.data.jpa.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
class SourceJpaRepository(private val sourceDAO: SourceDAO) : SourceRepository {
    override suspend fun setErrorState(
        id: SourceId,
        erroneous: Boolean,
        errorMessage: String?
    ) {
        withContext(Dispatchers.IO) {
            sourceDAO.setErrorState(id.uuid, erroneous, errorMessage)
        }
    }

    override suspend fun countByRepositoryIdAndLastRecordsRetrieved(
        repositoryId: RepositoryId,
        count: Int
    ): Int {
        return withContext(Dispatchers.IO) {
            sourceDAO.countByRepositoryIdAndLastRecordsRetrieved(repositoryId.uuid, count)
        }
    }

    override suspend fun findByIdWithActions(sourceId: SourceId): Source? {
        return withContext(Dispatchers.IO) {
            sourceDAO.findByIdWithActions(sourceId.uuid)?.toDomain()
        }
    }

    override suspend fun countByRepositoryId(id: RepositoryId): Long {
        return withContext(Dispatchers.IO) {
            sourceDAO.countByRepositoryId(id.uuid)
        }
    }

    override suspend fun findAllWithActionsByIdIn(ids: List<SourceId>): List<Source> {
        return withContext(Dispatchers.IO) {
            sourceDAO.findAllWithActionsByIdIn(ids.map { it.uuid }).map { it.toDomain() }
        }
    }

    override suspend fun findAllByRepositoryIdAndIdIn(
        repositoryId: RepositoryId,
        sourceIds: List<SourceId>
    ): List<Source> {
        return withContext(Dispatchers.IO) {
            sourceDAO.findAllByRepositoryIdAndIdIn(repositoryId.uuid, sourceIds.map { it.uuid })
                .map { it.toDomain() }
        }
    }
}