package org.migor.feedless.data.jpa.document

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Path
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.data.jpa.repository.toPageRequest
import org.migor.feedless.document.DatesWhereInput
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentDateField
import org.migor.feedless.document.DocumentFrequency
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.document.RecordOrderBy
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.source.SourceId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.document} & ${AppLayer.repository}")
class DocumentJpaRepository(private val documentDAO: DocumentDAO, private val entityManager: EntityManager) :
  DocumentRepository {

  override fun deleteAllByRepositoryIdAndStatusWithSkip(
    repositoryId: RepositoryId,
    status: ReleaseStatus,
    skip: Int
  ) {
    return documentDAO.deleteAllByRepositoryIdAndStatusWithSkip(repositoryId.uuid, status, skip)
  }

  override fun deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
    repositoryId: RepositoryId,
    date: LocalDateTime,
    status: ReleaseStatus
  ) {
    documentDAO.deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(repositoryId.uuid, date, status)

  }

  override fun deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
    id: RepositoryId,
    maxDate: LocalDateTime,
    released: ReleaseStatus
  ) {
    documentDAO.deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(id.uuid, maxDate, released)
  }

  override fun deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
    id: RepositoryId,
    maxDate: LocalDateTime,
    released: ReleaseStatus
  ) {
    documentDAO.deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(id.uuid, maxDate, released)

  }

  override fun findByTitleInAndRepositoryId(
    titles: List<String>,
    repositoryId: RepositoryId
  ): Document? {
    return documentDAO.findByTitleInAndRepositoryId(titles, repositoryId.uuid)?.toDomain()
  }

  override fun countByRepositoryId(id: RepositoryId): Long {
    return documentDAO.countByRepositoryId(id.uuid)
  }

  override fun findAllByRepositoryId(id: RepositoryId): List<Document> {
    return documentDAO.findAllByRepositoryId(id.uuid).map { it.toDomain() }
  }

  override fun findAllByRepositoryIdAndIdIn(
    repositoryId: RepositoryId,
    ids: List<DocumentId>
  ): List<Document> {
    return documentDAO.findAllByRepositoryIdAndIdIn(repositoryId.uuid, ids.map { it.uuid }).map { it.toDomain() }
  }

  override fun findAllBySourceId(
    sourceId: SourceId,
    pageable: PageableRequest
  ): List<Document> {
    return documentDAO.findAllBySourceId(sourceId.uuid, pageable.toPageRequest()).map { it.toDomain() }
  }

  override fun findByIdWithSource(documentId: DocumentId): Document? {
    return documentDAO.findByIdWithSource(documentId.uuid)?.toDomain()
  }

  override fun countBySourceId(sourceId: SourceId): Int {
    return documentDAO.countBySourceId(sourceId.uuid)
  }

  override fun findFirstByContentHashOrUrlAndRepositoryId(
    contentHash: String,
    url: String,
    repositoryId: RepositoryId
  ): Document? {
    return documentDAO.findFirstByContentHashOrUrlAndRepositoryId(contentHash, url, repositoryId.uuid)?.toDomain()
  }

  override fun findAllWithAttachmentsByIdIn(ids: List<DocumentId>): List<Document> {
    return documentDAO.findAllWithAttachmentsByIdIn(ids.map { it.uuid }).map { it.toDomain() }
  }

  override fun findById(id: DocumentId): Document? {
    return documentDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun deleteAllById(ids: List<DocumentId>) {
    documentDAO.deleteAllById(ids.map { it.uuid })
  }

  override fun save(document: Document): Document {
    return documentDAO.save(document.toEntity()).toDomain()
  }

  override fun deleteById(id: DocumentId) {
    documentDAO.deleteById(id.uuid)
  }

  override fun saveAll(documents: List<Document>): List<Document> {
    return documentDAO.saveAll(documents.map { it.toEntity() }).map { it.toDomain() }
  }

  override fun findAllFiltered(
    repositoryId: RepositoryId,
    filter: DocumentsFilter?,
    orderBy: RecordOrderBy?,
    status: ReleaseStatus,
    tags: List<String>,
    pageable: PageableRequest
  ): List<Document> {
    val query = jpql {
      val whereStatements = prepareWhereStatements(filter, tags)

      select(
        path(DocumentEntity::id),
      ).from(
        entity(DocumentEntity::class),
      )
        .whereAnd(
          path(DocumentEntity::repositoryId).eq(repositoryId.uuid),
          path(DocumentEntity::status).`in`(status),
          path(DocumentEntity::publishedAt).lt(LocalDateTime.now()),
          *whereStatements.toTypedArray()
        ).orderBy(
          orderBy?.let {
            path(DocumentEntity::startingAt).asc().nullsLast()
          } ?: path(DocumentEntity::publishedAt).desc()
        )
    }

    val context = JpqlRenderContext()

    val q = entityManager.createQuery(query, context)
    q.setMaxResults(pageable.pageSize)
    q.setFirstResult(pageable.pageSize * pageable.pageNumber)
    return documentDAO.findAllWithAttachmentsByIdIn(q.resultList.map { it }).map { it.toDomain() }
  }

  override fun getRecordFrequency(
    filter: DocumentsFilter,
    groupBy: DocumentDateField
  ): List<DocumentFrequency> {
    val query = jpql {
      val whereStatements = prepareWhereStatements(filter)
      val dateGroup = expression<Long>("day")

      val groupByEntity = when (groupBy) {
        DocumentDateField.createdAt -> path(DocumentEntity::createdAt)
        DocumentDateField.publishedAt -> path(DocumentEntity::publishedAt)
        DocumentDateField.startingAt -> path(DocumentEntity::startingAt)
      }

      selectNew<Pair<Long, Long>>(
        count(path(DocumentEntity::id)),
        function(Long::class, "fl_trunc_timestamp_as_millis", groupByEntity).`as`(dateGroup)
      ).from(
        entity(DocumentEntity::class),
      )
        .whereAnd(
          groupByEntity.isNotNull(),
          path(DocumentEntity::repositoryId).eq(filter.repository.uuid),
          path(DocumentEntity::publishedAt).lt(LocalDateTime.now()),
          *whereStatements.toTypedArray()
        )
        .groupBy(dateGroup)
    }

    val context = JpqlRenderContext()
    val q = entityManager.createQuery(query, context)
    return q.resultList.map { pair -> DocumentFrequency(pair.first.toInt(), pair.second) }
  }

  private fun prepareWhereStatements(
    where: DocumentsFilter?,
    tags: List<String> = emptyList()
  ): MutableList<Predicatable> {
    val whereStatements = mutableListOf<Predicatable>()
    jpql {
      val addDateConstraint = { it: DatesWhereInput, field: Path<LocalDateTime> ->
        it.before?.let {
          whereStatements.add(field.le(it))
        }
        it.after?.let {
          whereStatements.add(field.ge(it))
        }
        // todo create a test
        if (it.inFuture == true) {
          whereStatements.add(field.ge(LocalDateTime.now()))
        }
      }

      if (tags.isNotEmpty()) {
        // todo one of
      }

      where?.let {
        it.id?.eq?.let { whereStatements.add(path(DocumentEntity::id).eq(UUID.fromString(it))) }
        it.id?.`in`?.let { whereStatements.add(path(DocumentEntity::id).`in`(it.map { UUID.fromString(it) })) }
        it.source?.let { whereStatements.add(path(DocumentEntity::sourceId).eq(it.id.uuid)) }
        it.startedAt?.let { addDateConstraint(it, path(DocumentEntity::startingAt)) }
        it.createdAt?.let { addDateConstraint(it, path(DocumentEntity::createdAt)) }
        it.updatedAt?.let { addDateConstraint(it, path(DocumentEntity::updatedAt)) }
        it.publishedAt?.let { addDateConstraint(it, path(DocumentEntity::publishedAt)) }
        it.latLng?.let {
          whereStatements.add(path(DocumentEntity::latLon).isNotNull())
          it.near?.let {
            // https://postgis.net/docs/ST_Distance.html
            whereStatements.add(
              function(
                Double::class,
                "fl_latlon_distance",
                path(DocumentEntity::latLon),
                doubleLiteral(it.point.lat),
                doubleLiteral(it.point.lng)
              )
                .lt(doubleLiteral(it.distanceKm.coerceAtMost(50.0)))
            )
          }
        }
      }
      // dummy
      select(expression<String>("")).from(entity(DocumentEntity::class))
    }

    return whereStatements
  }

}
