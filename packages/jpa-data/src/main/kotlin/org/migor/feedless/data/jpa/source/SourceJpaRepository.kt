package org.migor.feedless.data.jpa.source

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Path
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.sort.Sortable
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.data.jpa.source.actions.FetchActionEntity
import org.migor.feedless.document.SortOrder
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceOrderBy
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourcesFilter
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
class SourceJpaRepository(private val sourceDAO: SourceDAO, private val entityManager: EntityManager) :
  SourceRepository {
  override fun setErrorState(
    id: SourceId,
    erroneous: Boolean,
    errorMessage: String?
  ) {
    sourceDAO.setErrorState(id.uuid, erroneous, errorMessage)
  }

  @Transactional
  override fun recordHarvestSucceeded(id: SourceId, recordsRetrieved: Int, refreshedAt: LocalDateTime) {
    sourceDAO.updateHarvestSucceeded(id.uuid, recordsRetrieved, refreshedAt)
  }

  @Transactional
  override fun recordHarvestFailed(id: SourceId, errorMessage: String?, refreshedAt: LocalDateTime) {
    sourceDAO.updateHarvestFailed(id.uuid, abbreviateErrorMessage(errorMessage), refreshedAt)
  }

  @Transactional
  override fun recordHarvestInterrupted(id: SourceId, errorMessage: String?, refreshedAt: LocalDateTime) {
    sourceDAO.updateHarvestInterrupted(id.uuid, abbreviateErrorMessage(errorMessage), refreshedAt)
  }

  // A bulk update bypasses SourceEntity.prePersist, which abbreviates the message on save.
  private fun abbreviateErrorMessage(errorMessage: String?): String? =
    StringUtils.abbreviate(errorMessage, LEN_STR_DEFAULT)

  override fun countSourcesWithProblems(
    repositoryId: RepositoryId,
  ): Int {
    return sourceDAO.countByRepositoryIdAndLastRecordsRetrieved(repositoryId.uuid, 0)
  }

  override fun findByIdWithActions(sourceId: SourceId): Source? {
    return sourceDAO.findByIdWithActions(sourceId.uuid)?.toDomain()
  }

  override fun countByRepositoryId(id: RepositoryId): Long {
    return sourceDAO.countByRepositoryId(id.uuid)
  }

  override fun findAllWithActionsByIdIn(ids: List<SourceId>): List<Source> {
    return sourceDAO.findAllWithActionsByIdIn(ids.map { it.uuid }).map { it.toDomain() }
  }

  override fun findAllByRepositoryIdAndIdIn(
    repositoryId: RepositoryId,
    sourceIds: List<SourceId>
  ): List<Source> {
    return sourceDAO.findAllByRepositoryIdAndIdIn(repositoryId.uuid, sourceIds.map { it.uuid })
      .map { it.toDomain() }
  }

  override fun save(source: Source): Source {
    return sourceDAO.save(source.toEntity()).toDomain()
  }

  override fun deleteAllById(ids: List<SourceId>) {
    sourceDAO.deleteAllById(ids.map { it.uuid })
  }

  override fun findById(id: SourceId): Source? {
    return sourceDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun saveAll(sources: List<Source>): List<Source> {
    return sourceDAO.saveAll(sources.map { it.toEntity() }).map { it.toDomain() }
  }

  override fun findAllByRepositoryIdFiltered(
    repositoryId: RepositoryId,
    pageable: PageableRequest,
    where: SourcesFilter?,
    orders: List<SourceOrderBy>?
  ): List<Source> {

    val whereStatements = mutableListOf<Predicatable>()
    val sortableStatements = mutableListOf<Sortable>()
    val query = jpql {
      where?.let {
        it.like?.let { like ->
          if (like.length > 2) {
            whereStatements.add(
              or(
                path(SourceEntity::title).like("%$like%"),
                path(FetchActionEntity::url).like("%$like%"),
              )
            )
          }
        }
        it.disabled?.let {
          whereStatements.add(
            path(SourceEntity::disabled).eq(it),
          )
        }
        it.minErrorsInSuccession?.let { min ->
          whereStatements.add(
            path(SourceEntity::errorsInSuccession).ge(min),
          )
        }
        it.id?.let {
          it.eq?.let {
            whereStatements.add(path(SourceEntity::id).eq(UUID.fromString(it)))
          }
          it.`in`?.let {
            whereStatements.add(path(SourceEntity::id).`in`(it.map { UUID.fromString(it) }))
          }
        }

        it.latLng?.let {
          // https://postgis.net/docs/ST_Distance.html
          whereStatements.add(path(SourceEntity::latLon).isNotNull())
          it.near?.let {
            whereStatements.add(
              function(
                Double::class,
                "fl_latlon_distance",
                path(SourceEntity::latLon),
                doubleLiteral(it.point.lat),
                doubleLiteral(it.point.lng)
              )
                .lt(doubleLiteral(it.distanceKm.coerceAtMost(20.0)))
            )
          }
        }
      }
      val applySortDirection = { path: Path<*>, direction: SortOrder ->
        when (direction) {
          SortOrder.ASC -> path.asc().nullsFirst()
          SortOrder.DESC -> path.desc().nullsLast()
        }
      }

      orders?.let {
        sortableStatements.addAll(
          orders.map {
            if (it.title != null) {
              applySortDirection(path(SourceEntity::title), it.title!!)
            } else {
              if (it.lastRecordsRetrieved != null) {
                applySortDirection(path(SourceEntity::lastRecordsRetrieved), it.lastRecordsRetrieved!!)
              } else {
                if (it.lastRefreshedAt != null) {
                  applySortDirection(path(SourceEntity::lastRefreshedAt), it.lastRefreshedAt!!)
                } else {
                  throw IllegalArgumentException("Underspecified source order params")
                }
              }
            }
          }
        )
      }

      select(path(SourceEntity::id))
        .from(
          entity(SourceEntity::class),
          join(FetchActionEntity::class).on(path(FetchActionEntity::sourceId).eq(path(SourceEntity::id)))
        )
        .whereAnd(
          path(SourceEntity::repositoryId).eq(repositoryId.uuid),
          *whereStatements.toTypedArray(),
        )
        .orderBy(
          *sortableStatements.toTypedArray(),
          path(SourceEntity::createdAt).desc(),
          // createdAt alone isn't unique; without a tiebreaker pagination repeats or drops rows.
          path(SourceEntity::id).asc(),
        )
    }

    val context = JpqlRenderContext()

    val q = entityManager.createQuery(query, context)
    // offset uses the true pageSize, so fetching one extra never shifts it.
    q.setMaxResults(pageable.limit)
    q.setFirstResult(pageable.offset)
    // The IN-fetch loses order; restore it so the caller's take(pageSize) drops the right row.
    val orderedIds = q.resultList
    val byId = sourceDAO.findAllWithActionsByIdIn(orderedIds).associateBy { it.id }
    return orderedIds.mapNotNull { byId[it] }.map { it.toDomain() }
  }

  override fun findAllForUser(
    userId: UserId,
    groupIds: List<GroupId>,
    pageable: PageableRequest,
    where: SourcesFilter?,
  ): List<Source> {
    val whereStatements = mutableListOf<Predicatable>()
    val query = jpql {
      where?.let {
        it.like?.let { like ->
          if (like.length > 2) {
            whereStatements.add(
              or(
                path(SourceEntity::title).like("%$like%"),
                path(FetchActionEntity::url).like("%$like%"),
              )
            )
          }
        }
        it.disabled?.let {
          whereStatements.add(
            path(SourceEntity::disabled).eq(it),
          )
        }
        it.minErrorsInSuccession?.let { min ->
          whereStatements.add(
            path(SourceEntity::errorsInSuccession).ge(min),
          )
        }
      }

      // Owner or group member, as a query predicate rather than a post-filter, so pagination stays correct.
      val accessPredicate = if (groupIds.isEmpty()) {
        path(RepositoryEntity::ownerId).eq(userId.uuid)
      } else {
        or(
          path(RepositoryEntity::ownerId).eq(userId.uuid),
          path(RepositoryEntity::groupId).`in`(groupIds.map { it.uuid }),
        )
      }

      select(path(SourceEntity::id))
        .from(
          entity(SourceEntity::class),
          join(FetchActionEntity::class).on(path(FetchActionEntity::sourceId).eq(path(SourceEntity::id))),
          join(RepositoryEntity::class).on(path(RepositoryEntity::id).eq(path(SourceEntity::repositoryId))),
        )
        .whereAnd(
          accessPredicate,
          *whereStatements.toTypedArray(),
        )
        .orderBy(
          path(SourceEntity::errorsInSuccession).desc(),
          path(SourceEntity::lastRefreshedAt).desc().nullsLast(),
          // Neither key is unique; without a tiebreaker pagination repeats or drops rows.
          path(SourceEntity::createdAt).desc(),
          path(SourceEntity::id).asc(),
        )
    }

    val context = JpqlRenderContext()

    val q = entityManager.createQuery(query, context)
    // offset uses the true pageSize, so fetching one extra never shifts it.
    q.setMaxResults(pageable.limit)
    q.setFirstResult(pageable.offset)
    // The IN-fetch below does not preserve order, so re-apply the query's own ordering afterwards.
    val orderedIds = q.resultList
    val byId = sourceDAO.findAllWithActionsByIdIn(orderedIds).associateBy { it.id }
    return orderedIds.mapNotNull { byId[it] }.map { it.toDomain() }
  }
}
