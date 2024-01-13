package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile(AppProfiles.database)
class RetentionStrategyService {

  private val log = LoggerFactory.getLogger(RetentionStrategyService::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  fun applyRetentionStrategy(corrId: String, subscription: SourceSubscriptionEntity) {
    subscription.retentionMaxItems?.let {retentionSize ->
      log.info("applying retention with maxItems=$retentionSize")
      webDocumentDAO.deleteAllBySubscriptionIdAndStatusWithSkip(subscription.id, ReleaseStatus.released, retentionSize)
    }

    subscription.retentionMaxAgeDays?.let {maxAgeDays ->
      log.info("applying retention with maxAgeDays=$maxAgeDays")
      val maxDate = Date.from(LocalDateTime.now().minus(maxAgeDays.toLong(), ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant())
      webDocumentDAO.deleteAllBySubscriptionIdAndCreatedAtBeforeAndStatus(subscription.id, maxDate, ReleaseStatus.released)
    }
  }
}
