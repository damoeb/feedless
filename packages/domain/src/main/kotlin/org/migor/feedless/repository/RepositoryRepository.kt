package org.migor.feedless.repository

import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.Vertical
import org.migor.feedless.document.DocumentId
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface RepositoryRepository {

  suspend fun findAll(pageable: PageableRequest, where: RepositoriesFilter?, userId: UserId?): List<Repository>

  suspend fun findAllWhereNextHarvestIsDue(now: LocalDateTime, pageable: PageableRequest): List<Repository>

  suspend fun countByOwnerId(id: UserId): Int

  suspend fun countByOwnerIdAndArchivedIsFalseAndSourcesSyncCronIsNot(id: UserId, cron: String): Int
  suspend fun countAllByOwnerIdAndProduct(id: UserId, product: Vertical): Int
  suspend fun countAllByVisibility(visibility: EntityVisibility): Int
  suspend fun findByTitleAndOwnerId(title: String, ownerId: UserId): Repository?

  suspend fun findAllByVisibilityAndLastPullSyncBefore(
    visibility: EntityVisibility,
    now: LocalDateTime?,
    pageable: PageableRequest
  ): List<Repository>

  suspend fun findInboxRepositoryByUserId(userId: UserId)

  suspend fun findBySourceId(sourceId: SourceId): Repository?

  suspend fun findByDocumentId(documentId: DocumentId): Repository?
  suspend fun findAllByLastUpdatedAtBefore(lastUpdatedAt: LocalDateTime): List<Repository>
  suspend fun findById(id: RepositoryId): Repository?
  suspend fun save(repository: Repository): Repository
  suspend fun delete(repository: Repository)

}
