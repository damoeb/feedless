package org.migor.feedless.document

import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expression
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.generated.types.StringFilter
import org.migor.feedless.generated.types.WebDocumentOrderByInput
import org.migor.feedless.generated.types.WebDocumentsWhereInput
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.repository.MaxAgeDaysDateField
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.jvm.optionals.getOrNull


data class FrequencyItem(val year: Int, val month: Int, val day: Int, val count: Int)


@Service
@Profile(AppProfiles.database)
class DocumentService {

  private val log = LoggerFactory.getLogger(DocumentService::class.simpleName)

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var planConstraintsService: PlanConstraintsService

  fun findById(id: UUID): DocumentEntity? {
    return documentDAO.findById(id).getOrNull()
  }

  fun findAllByRepositoryId(
    repositoryId: UUID,
    where: WebDocumentsWhereInput? = null,
    orderBy: WebDocumentOrderByInput? = null,
    status: ReleaseStatus = ReleaseStatus.released,
    tag: String? = null,
    pageable: Pageable,
    shareKey: String? = null
  ): Page<DocumentEntity?> {
    val repo = repositoryDAO.findById(repositoryId).orElseThrow()

    if (repo.visibility !== EntityVisibility.isPublic && repo.ownerId != sessionService.userId() && repo.shareKey != shareKey) {
      throw IllegalArgumentException("repo is not public")
    }

    return documentDAO.findPage(pageable) {
      val whereStatements = mutableListOf<Predicatable>()

      where?.let {
        it.startedAt?.let {
          it.before?.let {
            whereStatements.add(path(DocumentEntity::startingAt).le(Date(it)))
          }
          it.after?.let {
            whereStatements.add(path(DocumentEntity::startingAt).ge(Date(it)))
          }
        }
        it.localized?.let {
          // https://postgis.net/docs/ST_Distance.html
//          val point = function(Point::class,"ST_Point", it.near.lat, it.near.lon)
//          whereStatements.add(function(Double::class, "ST_Distance", path(DocumentEntity::latLon), point).ge(0.0))

          val st_point = { lat: Double, lon: Double -> function(Point::class, "st_point", lat, lon) }
          val st_setsrid =
            { geom: Expression<Geometry>, srid: Int -> function(Geometry::class, "ST_SetSRID", geom, srid) }
          val st_transform =
            { geom: Expression<Geometry>, to_srid: Int -> function(Geometry::class, "st_transform", geom, to_srid) }
          val st_distance = { geom1: Expression<Geometry>, geom2: Expression<Geometry> ->
            function(
              Double::class,
              "st_distance",
              geom1,
              geom2
            )
          }

//          whereStatements.add(
//            st_distance(
//              st_transform(
//                st_setsrid(path(DocumentEntity::latLon), 4326),
//                3857),
//              st_transform(
//                st_setsrid(st_point(it.near.lat, it.near.lat), 4326),
//                3857)).ge(0.0)
//          )
          whereStatements.add(
            st_setsrid(st_point(it.near.lat, it.near.lat), 4326).isNotNull()
          )
        }
      }

      select(
        entity(DocumentEntity::class),
      ).from(
        entity(DocumentEntity::class)
      ).whereAnd(
        path(DocumentEntity::repositoryId).eq(repositoryId),
        path(DocumentEntity::status).`in`(status),
        path(DocumentEntity::publishedAt).lt(Date()),
        *whereStatements.toTypedArray()
      ).orderBy(
        orderBy?.let {
          path(DocumentEntity::startingAt).asc().nullsLast()
        } ?: path(DocumentEntity::publishedAt).desc()
      )
    }
  }

  fun applyRetentionStrategy(corrId: String, repository: RepositoryEntity) {
    val retentionSize =
      planConstraintsService.coerceRetentionMaxCapacity(
        repository.retentionMaxCapacity,
        repository.ownerId,
        repository.product
      )
    if (retentionSize != null && retentionSize > 0) {
      log.info("[$corrId] applying retention with maxItems=$retentionSize")
      documentDAO.deleteAllByRepositoryIdAndStatusWithSkip(repository.id, ReleaseStatus.released, retentionSize)
    } else {
      log.info("[$corrId] no retention with maxItems given")
    }

    planConstraintsService.coerceRetentionMaxAgeDays(
      repository.retentionMaxAgeDays,
      repository.ownerId,
      repository.product
    )
      ?.let { maxAgeDays ->
        log.info("[$corrId] applying retention with maxAgeDays=$maxAgeDays")
        val maxDate = Date.from(
          LocalDateTime.now().minus(maxAgeDays.toLong(), ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant()
        )
        if (repository.retentionMaxAgeDaysReferenceField == MaxAgeDaysDateField.startingAt) {
          documentDAO.deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(
            repository.id,
            maxDate,
            ReleaseStatus.released
          )
        } else {
          documentDAO.deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
            repository.id,
            maxDate,
            ReleaseStatus.released
          )
        }

      } ?: log.info("[$corrId] no retention with maxAgeDays given")
  }

  fun deleteDocuments(corrId: String, user: UserEntity, repositoryId: UUID, documentIds: StringFilter) {
    log.info("[$corrId] deleteDocuments $documentIds")
    val repository = repositoryDAO.findById(repositoryId).orElseThrow()
    if (repository.ownerId != user.id) {
      throw PermissionDeniedException("current user ist not owner ($corrId)")
    }

    if (documentIds.`in` != null) {
      documentDAO.deleteAllByRepositoryIdAndIdIn(repositoryId, documentIds.`in`.map { UUID.fromString(it) })
    } else {
//      if (documentIds.notIn != null) {
//        documentDAO.deleteAllByRepositoryIdAndIdNotIn(repositoryId, documentIds.notIn`.map { UUID.fromString(it) })
//      } else {
      if (documentIds.equals != null) {
        documentDAO.deleteAllByRepositoryIdAndId(repositoryId, UUID.fromString(documentIds.equals))
      } else {
        throw IllegalArgumentException("operation not supported")
      }
//      }

    }
  }

  fun getDocumentFrequency(
    repositoryId: UUID,
  ): List<FrequencyItem> {
    return documentDAO.histogramPerDayByStreamIdOrImporterId(repositoryId)
      .map {
        FrequencyItem(
          year = (it[0] as Double).toInt(),
          month = (it[1] as Double).toInt(),
          day = (it[2] as Double).toInt(),
          count = (it[3] as Long).toInt(),
        )
      }
  }
}
