package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.DocumentEntity
import org.migor.feedless.data.jpa.models.RepositoryEntity
import org.migor.feedless.data.jpa.repositories.DocumentDAO
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*


data class FrequencyItem(val year: Int, val month: Int, val day: Int, val count: Int)


@Service
@Profile(AppProfiles.database)
class DocumentService {

  private val log = LoggerFactory.getLogger(DocumentService::class.simpleName)

  @Autowired
  lateinit var documentDAO: DocumentDAO

  @Autowired
  lateinit var planConstraintsService: PlanConstraintsService

  fun findById(id: UUID): Optional<DocumentEntity> {
    return documentDAO.findById(id)
  }

  fun findAllByRepositoryId(
    repositoryId: UUID,
    page: Int?,
    pageSize: Int? = null,
    status: ReleaseStatus = ReleaseStatus.released
  ): List<DocumentEntity> {
    val fixedPage = (page ?: 0).coerceAtLeast(0)
    val fixedPageSize = (pageSize ?: 0).coerceAtLeast(1).coerceAtMost(50)
    val pageable = PageRequest.of(fixedPage, fixedPageSize, Sort.by(Sort.Direction.DESC, "publishedAt"))
    return documentDAO.findAllByRepositoryIdAndStatusAndPublishedAtBefore(repositoryId, status, Date(), pageable)
  }

  fun applyRetentionStrategy(corrId: String, repository: RepositoryEntity) {
    val retentionSize =
      planConstraintsService.coerceRetentionMaxItems(repository.retentionMaxItems, repository.ownerId)
    if (retentionSize != null && retentionSize > 0) {
      log.info("[$corrId] applying retention with maxItems=$retentionSize")
      documentDAO.deleteAllByRepositoryIdAndStatusWithSkip(repository.id, ReleaseStatus.released, retentionSize)
    } else {
      log.info("[$corrId] no retention with maxItems given")
    }


    planConstraintsService.coerceRetentionMaxAgeDays(repository.retentionMaxAgeDays)
      ?.let { maxAgeDays ->
        log.info("[$corrId] applying retention with maxAgeDays=$maxAgeDays")
        val maxDate = Date.from(
          LocalDateTime.now().minus(maxAgeDays.toLong(), ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant()
        )
        documentDAO.deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(
          repository.id,
          maxDate,
          ReleaseStatus.released
        )
      } ?: log.info("[$corrId] no retention with maxAgeDays given")
  }

  fun deleteDocumentById(corrId: String, user: UserEntity, id: UUID) {
    documentDAO.deleteByIdAndOwnerId(id, user.id)
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
