package org.migor.feedless.harvest

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.ScrapeSourceEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.generated.types.ScrapedByBoundingBox
import org.migor.feedless.generated.types.ScrapedBySelector
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.ScrapeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
@Profile(AppProfiles.database)
class SourceSubscriptionHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(SourceSubscriptionHarvester::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var scrapeService: ScrapeService

  @Autowired
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun handleSourceSubscription(corrId: String, subscription: SourceSubscriptionEntity) {
    log.info("[${corrId}] handleSourceSubscription ${subscription.id}")
    try {
      updateScheduledNextAt(corrId, subscription)
      if (subscription.triggerScheduledNextAt == null) {
        log.info("[$corrId] is unscheduled yet")
      } else {
        val appendedCount = scrapeSources(corrId, subscription)
        val now = Date()
        log.info("[$corrId] Appended segment of size $appendedCount to bucket ${subscription.bucketId}")

        sourceSubscriptionDAO.setLastUpdatedAt(subscription.id, now)
      }

    } catch (e: Exception) {
      log.error("[$corrId] Cannot update scheduled bucket ${subscription.id}: ${e.message}")
    }
  }

  private fun updateScheduledNextAt(corrId: String, subscription: SourceSubscriptionEntity) {
    val scheduledNextAt =
      Date.from(CronExpression.parse(subscription.schedulerExpression).next(LocalDateTime.now())!!.toInstant(ZoneOffset.UTC))
    log.info("[$corrId] Next import scheduled for $scheduledNextAt")
    sourceSubscriptionDAO.setScheduledNextAt(subscription.id, scheduledNextAt)
  }

  private fun scrapeSources(
    corrId: String,
    subscription: SourceSubscriptionEntity
  ) {
    subscription.sources.forEach {
      scrapeSource(corrId, it)
    }

//    val defaultScheduledLastAt = Date.from(
//      LocalDateTime.now().minus(1, ChronoUnit.MONTHS).toInstant(
//        ZoneOffset.UTC
//      )
//    )
//
//    val segmentSize = importer.segmentSize ?: 100
//    val segmentSortField = importer.segmentSortField ?: "score"
//    val segmentSortOrder = if (importer.segmentSortAsc) {
//      Sort.Order.asc(segmentSortField)
//    } else {
//      Sort.Order.desc(segmentSortField)
//    }
//    val pageable = PageRequest.of(0, segmentSize, Sort.by(segmentSortOrder))
//    val articles = webDocumentDAO.findAllThrottled(
//      importer.feedId,
//      importer.triggerScheduledLastAt ?: defaultScheduledLastAt,
//      pageable
//    )
//
//    refineAndImportArticlesScheduled(corrId, articles, importer)
  }

  private fun scrapeSource(corrId: String, source: ScrapeSourceEntity): Long? {
    return scrapeService.scrape(corrId, source.scrapeRequest)
      .flatMapMany { scrapeResponse -> Flux.fromIterable(scrapeResponse.elements) }
      .map { importElement(it) }
      .count()
      .block()
  }

  private fun importElement(element: ScrapedElement) {
    element.image?.let {
      importImageElement(it)
    }
    element.selector?.let {
      importSelectorElement(it)
    }
  }

  private fun importSelectorElement(it: ScrapedBySelector) {

  }

  private fun importImageElement(it: ScrapedByBoundingBox) {
    TODO("Not yet implemented")
  }
}
