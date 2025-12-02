package org.migor.feedless.repository

import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.Vertical
import org.migor.feedless.document.DocumentId
import org.migor.feedless.group.GroupId
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

interface RepositoryRepository {

  fun findAll(pageable: PageableRequest, where: RepositoriesFilter?, userId: UserId?): List<Repository>

  fun findAllWhereNextHarvestIsDue(now: LocalDateTime, pageable: PageableRequest): List<Repository>

  fun countByGroupId(id: GroupId): Int

  fun countByGroupIdAndArchivedIsFalseAndSourcesSyncCronIsNot(groupId: GroupId, cron: String): Int
  fun countAllByOwnerIdAndProduct(id: UserId, product: Vertical): Int
  fun countAllByVisibility(visibility: EntityVisibility): Int
  fun findByTitleAndOwnerId(title: String, ownerId: UserId): Repository?

  fun findAllByVisibilityAndLastPullSyncBefore(
    visibility: EntityVisibility,
    now: LocalDateTime?,
    pageable: PageableRequest
  ): List<Repository>

  fun findInboxRepositoryByUserId(userId: UserId)

  fun findBySourceId(sourceId: SourceId): Repository?

  fun findByDocumentId(documentId: DocumentId): Repository?
  fun findAllByLastUpdatedAtBefore(lastUpdatedAt: LocalDateTime): List<Repository>
  fun findById(id: RepositoryId): Repository?
  fun save(repository: Repository): Repository
  fun delete(repository: Repository)

}
