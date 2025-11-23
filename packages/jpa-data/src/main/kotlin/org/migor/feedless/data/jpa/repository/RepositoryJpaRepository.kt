package org.migor.feedless.data.jpa.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.Vertical
import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
class RepositoryJpaRepository(private val repositoryDAO: RepositoryDAO) : RepositoryRepository {
    override suspend fun findAllWhereNextHarvestIsDue(
        now: LocalDateTime,
        pageable: PageableRequest
    ): List<Repository> {
        return withContext(Dispatchers.IO) {
            repositoryDAO.findAllWhereNextHarvestIsDue(now, pageable.toPageRequest()).map { it.toDomain() }
        }
    }

    override suspend fun countByOwnerId(id: UserId): Int {
        return withContext(Dispatchers.IO) {
            repositoryDAO.countByOwnerId(id.uuid)
        }
    }

    override suspend fun countByOwnerIdAndArchivedIsFalseAndSourcesSyncCronIsNot(
        id: UserId,
        cron: String
    ): Int {
        return withContext(Dispatchers.IO) {
            repositoryDAO.countByOwnerIdAndArchivedIsFalseAndSourcesSyncCronIsNot(id.uuid, cron)
        }
    }

    override suspend fun countAllByOwnerIdAndProduct(
        id: UserId,
        product: Vertical
    ): Int {
        return withContext(Dispatchers.IO) {
            repositoryDAO.countAllByOwnerIdAndProduct(id.uuid, product)
        }
    }

    override suspend fun countAllByVisibility(visibility: EntityVisibility): Int {
        return withContext(Dispatchers.IO) {
            repositoryDAO.countAllByVisibility(visibility)
        }
    }

    override suspend fun findByTitleAndOwnerId(
        title: String,
        ownerId: UserId
    ): Repository? {
        return withContext(Dispatchers.IO) {
            repositoryDAO.findByTitleAndOwnerId(title, ownerId.uuid)?.toDomain()
        }
    }

    override suspend fun findAllByVisibilityAndLastPullSyncBefore(
        visibility: EntityVisibility,
        now: LocalDateTime?,
        pageable: PageableRequest
    ): List<Repository> {
        return withContext(Dispatchers.IO) {
            repositoryDAO.findAllByVisibilityAndLastPullSyncBefore(visibility, now, pageable.toPageRequest())
                .map { it.toDomain() }
        }
    }

    override suspend fun findInboxRepositoryByUserId(userId: UserId) {
        TODO("not implemented")
    }

    override suspend fun findBySourceId(sourceId: SourceId): Repository? {
        return withContext(Dispatchers.IO) {
            repositoryDAO.findBySourceId(sourceId.uuid)?.toDomain()
        }
    }

    override suspend fun findByDocumentId(documentId: DocumentId): Repository? {
        return withContext(Dispatchers.IO) {
            repositoryDAO.findByDocumentId(documentId.uuid)?.toDomain()
        }
    }

    override suspend fun findAllByLastUpdatedAtBefore(lastUpdatedAt: LocalDateTime): List<Repository> {
        return withContext(Dispatchers.IO) {
            repositoryDAO.findAllByLastUpdatedAtBefore(lastUpdatedAt).map { it.toDomain() }
        }
    }

    override suspend fun findById(id: RepositoryId): Repository? {
        return withContext(Dispatchers.IO) {
            repositoryDAO.findById(id.uuid).getOrNull()?.toDomain()
        }
    }

    override suspend fun save(repository: Repository): Repository {
        return withContext(Dispatchers.IO) {
            repositoryDAO.save(repository.toEntity()).toDomain()
        }
    }

}

fun PageableRequest.toPageRequest(): PageRequest {
    TODO("Not yet implemented")
}
