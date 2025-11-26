package org.migor.feedless.data.jpa.repository

import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.Vertical
import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoriesFilter
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
class RepositoryJpaRepository(private val repositoryDAO: RepositoryDAO) : RepositoryRepository {

  override suspend fun findAll(
    pageable: PageableRequest,
    where: RepositoriesFilter?,
    userId: UserId?
  ): List<Repository> {
    return withContext(Dispatchers.IO) {
      repositoryDAO.findPage(pageable.toPageRequest()) {
        val whereStatements = mutableListOf<Predicatable>()
        where?.let {
          where.visibility?.let { visibility ->
            visibility.`in`?.let {
              whereStatements.add(
                path(RepositoryEntity::visibility).`in`(it),
              )
            }
          }

          userId?.let {
            whereStatements.add(
              path(RepositoryEntity::ownerId).eq(userId.uuid)
            )
          }

          where.product?.let {
            it.eq?.let {
              whereStatements.add(
                path(RepositoryEntity::product).eq(it)
              )
            }
            it.`in`?.let { products ->
              whereStatements.add(
                path(RepositoryEntity::product).`in`(products)
              )
            }
          }
          where.tags?.let {
            it.every?.let { every ->
              whereStatements.add(
                function(
                  Boolean::class,
                  "fl_array_contains",
                  path(RepositoryEntity::tags),
                  every,
                  true
                )
                  .eq(true)
              )
            }
            it.some?.let { some ->
              whereStatements.add(
                function(
                  Boolean::class,
                  "fl_array_contains",
                  path(RepositoryEntity::tags),
                  some,
                  false
                )
                  .eq(true)
              )
            }
          }
        }

        select(
          entity(RepositoryEntity::class)
        ).from(
          entity(RepositoryEntity::class)
        ).whereAnd(
          *whereStatements.toTypedArray(),
          or(
            path(RepositoryEntity::visibility).eq(EntityVisibility.isPublic),
            path(RepositoryEntity::ownerId).eq(userId?.uuid),
          )
        ).orderBy(
          path(RepositoryEntity::lastUpdatedAt).desc()
        )
      }.toList().filterNotNull().map { it.toDomain() }
    }
  }

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

  override suspend fun delete(repository: Repository) {
    withContext(Dispatchers.IO) {
      repositoryDAO.delete(repository.toEntity())
    }
  }

}

fun PageableRequest.toPageRequest(): PageRequest {
  val sort = if (sortBy.isEmpty()) {
    Sort.unsorted()
  } else {
    Sort.by(
      sortBy.map {
        if (it.asc) {
          Sort.Order.asc(it.field)
        } else {
          Sort.Order.desc(it.field)
        }
      }
    )
  }
  return PageRequest.of(pageNumber, pageSize, sort)
}
