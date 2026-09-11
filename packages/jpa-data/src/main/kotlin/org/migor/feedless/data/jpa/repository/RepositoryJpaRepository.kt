package org.migor.feedless.data.jpa.repository

import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.Vertical
import org.migor.feedless.document.DocumentId
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.RepositoriesFilter
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.source.SourceId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
class RepositoryJpaRepository(private val repositoryDAO: RepositoryDAO) : RepositoryRepository {

  override fun findAll(
    pageable: PageableRequest,
    where: RepositoriesFilter?,
    userId: UserId?
  ): List<Repository> {
    return repositoryDAO.findPage(pageable.toPageRequest()) {
      select(
        entity(RepositoryEntity::class)
      ).from(
        entity(RepositoryEntity::class)
      ).whereAnd(
        *repositoriesWhereStatements(where, userId).toTypedArray(),
        or(
          path(RepositoryEntity::visibility).eq(EntityVisibility.isPublic),
          path(RepositoryEntity::ownerId).eq(userId?.uuid),
        )
      ).orderBy(
        path(RepositoryEntity::lastUpdatedAt).desc(),
        // Tiebreaker: lastUpdatedAt alone is not unique (bulk creates can share a timestamp),
        // and without one, LIMIT/OFFSET pagination can repeat or drop rows across pages.
        path(RepositoryEntity::id).asc(),
      )
    }.toList().filterNotNull().map { it.toDomain() }
  }

  override fun countAllByUserId(where: RepositoriesFilter?, userId: UserId?): Int {
    return repositoryDAO.findAll {
      select(
        count(RepositoryEntity::id)
      ).from(
        entity(RepositoryEntity::class)
      ).whereAnd(
        *repositoriesWhereStatements(where, userId).toTypedArray(),
        or(
          path(RepositoryEntity::visibility).eq(EntityVisibility.isPublic),
          path(RepositoryEntity::ownerId).eq(userId?.uuid),
        )
      )
    }.firstOrNull()?.toInt() ?: 0
  }

  /** Predicates shared by [findAll] and [countAllByUserId] — keeps totalCount consistent with the list. */
  private fun Jpql.repositoriesWhereStatements(
    where: RepositoriesFilter?,
    userId: UserId?,
  ): MutableList<Predicatable> {
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

      where.text?.query?.takeIf { it.isNotBlank() }?.let { query ->
        // Case-insensitive substring match on title OR description. '%'/'_' are LIKE
        // wildcards — escape them (and the escape char itself) so the user's text matches
        // literally rather than being interpreted as a pattern.
        val likeEscapeChar = '\\'
        val escaped = query
          .replace("$likeEscapeChar", "$likeEscapeChar$likeEscapeChar")
          .replace("%", "${likeEscapeChar}%")
          .replace("_", "${likeEscapeChar}_")
        val pattern = "%${escaped.lowercase()}%"
        whereStatements.add(
          or(
            lower(path(RepositoryEntity::title)).like(pattern, likeEscapeChar),
            lower(path(RepositoryEntity::description)).like(pattern, likeEscapeChar),
          )
        )
      }
    }
    return whereStatements
  }

  override fun findAllWhereNextHarvestIsDue(
    now: LocalDateTime,
    pageable: PageableRequest
  ): List<Repository> {
    return repositoryDAO.findAllWhereNextHarvestIsDue(now, pageable.toPageRequest()).map { it.toDomain() }
  }

  override fun countByGroupId(id: GroupId): Int {
    return repositoryDAO.countByGroupId(id.uuid)
  }


  override fun countByGroupIdAndArchivedIsFalseAndSourcesSyncCronIsNot(
    groupId: GroupId,
    cron: String
  ): Int {
    // todo implement
//    return repositoryDAO.countByOwnerIdAndArchivedIsFalseAndSourcesSyncCronIsNot(userId.uuid, cron)
    return 0
  }

  override fun countAllByOwnerIdAndProduct(
    id: UserId,
    product: Vertical
  ): Int {
    return repositoryDAO.countAllByOwnerIdAndProduct(id.uuid, product)
  }

  override fun countAllByVisibility(visibility: EntityVisibility): Int {
    return repositoryDAO.countAllByVisibility(visibility)
  }

  override fun findByTitleAndOwnerId(
    title: String,
    ownerId: UserId
  ): Repository? {
    return repositoryDAO.findByTitleAndOwnerId(title, ownerId.uuid)?.toDomain()
  }

  override fun findAllByVisibilityAndLastPullSyncBefore(
    visibility: EntityVisibility,
    now: LocalDateTime?,
    pageable: PageableRequest
  ): List<Repository> {
    return repositoryDAO.findAllByVisibilityAndLastPullSyncBefore(visibility, now, pageable.toPageRequest())
      .map { it.toDomain() }
  }

  override fun findInboxRepositoryByUserId(userId: UserId) {
    TODO("not implemented")
  }

  override fun findBySourceId(sourceId: SourceId): Repository? {
    return repositoryDAO.findBySourceId(sourceId.uuid)?.toDomain()
  }

  override fun findByDocumentId(documentId: DocumentId): Repository? {
    return repositoryDAO.findByDocumentId(documentId.uuid)?.toDomain()
  }

  override fun findAllByLastUpdatedAtBefore(lastUpdatedAt: LocalDateTime): List<Repository> {
    return repositoryDAO.findAllByLastUpdatedAtBefore(lastUpdatedAt).map { it.toDomain() }
  }

  override fun findById(id: RepositoryId): Repository? {
    return repositoryDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun save(repository: Repository): Repository {
    return repositoryDAO.save(repository.toEntity()).toDomain()
  }

  override fun delete(repository: Repository) {
    repositoryDAO.delete(repository.toEntity())
  }

}

/**
 * A [PageRequest] when [PageableRequest.limit] is the plain [PageableRequest.pageSize] (the
 * overwhelming majority of callers) — a [OffsetLimitPageRequest] otherwise, so a request built
 * with [PageableRequest.withExtraForHasMore] fetches one extra row without shifting [offset].
 */
fun PageableRequest.toPageRequest(): Pageable {
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
  return if (limit == pageSize) {
    PageRequest.of(pageNumber, pageSize, sort)
  } else {
    OffsetLimitPageRequest(offset.toLong(), limit, sort)
  }
}
