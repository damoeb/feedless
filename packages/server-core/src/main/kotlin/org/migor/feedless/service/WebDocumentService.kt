package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
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
class WebDocumentService {

  private val log = LoggerFactory.getLogger(WebDocumentService::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var planConstraintsService: PlanConstraintsService

  fun findById(id: UUID): Optional<WebDocumentEntity> {
    return webDocumentDAO.findById(id)
  }

  fun findAllBySubscriptionId(
    subscriptionId: UUID,
    page: Int?,
    status: ReleaseStatus = ReleaseStatus.released
  ): List<WebDocumentEntity> {
    val pageable = PageRequest.of(page ?: 0, 10, Sort.by(Sort.Direction.DESC, StandardJpaFields.releasedAt))
    return webDocumentDAO.findAllBySubscriptionIdAndStatusAndReleasedAtBefore(subscriptionId, status, Date(), pageable)
  }

  fun applyRetentionStrategy(corrId: String, subscription: SourceSubscriptionEntity) {
    val retentionSize =
      planConstraintsService.coerceRetentionMaxItems(subscription.retentionMaxItems, subscription.ownerId)
    log.info("[$corrId] applying retention with maxItems=$retentionSize")
    if (retentionSize != null && retentionSize > 0) {
      webDocumentDAO.deleteAllBySubscriptionIdAndStatusWithSkip(subscription.id, ReleaseStatus.released, retentionSize)
    }

    planConstraintsService.coerceRetentionMaxAgeDays(subscription.retentionMaxAgeDays)
      ?.let { maxAgeDays ->
        log.info("[$corrId] applying retention with maxAgeDays=$maxAgeDays")
        val maxDate = Date.from(
          LocalDateTime.now().minus(maxAgeDays.toLong(), ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant()
        )
        webDocumentDAO.deleteAllBySubscriptionIdAndCreatedAtBeforeAndStatus(
          subscription.id,
          maxDate,
          ReleaseStatus.released
        )
      } ?: log.info("[$corrId] no retention with maxAgeDays given")
  }

  fun deleteWebDocumentById(corrId: String, user: UserEntity, id: UUID) {
    webDocumentDAO.deleteByIdAndOwnerId(id, user.id)
  }

  fun getWebDocumentFrequency(
    sourceSubscriptionId: UUID,
  ): List<FrequencyItem> {
    return webDocumentDAO.histogramPerDayByStreamIdOrImporterId(sourceSubscriptionId)
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
