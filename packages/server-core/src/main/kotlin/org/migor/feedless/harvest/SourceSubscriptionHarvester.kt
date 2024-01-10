package org.migor.feedless.harvest

import io.micrometer.core.instrument.MeterRegistry
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.ScrapeSourceEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.data.jpa.repositories.StreamDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapedByBoundingBox
import org.migor.feedless.generated.types.ScrapedBySelector
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
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
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var meterRegistry: MeterRegistry

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
        log.info("[$corrId] Appended $appendedCount items to bucket ${subscription.bucketId}")

        sourceSubscriptionDAO.setLastUpdatedAt(subscription.id, now)
      }

    } catch (e: Exception) {
      log.error("[$corrId] Cannot update scheduled subscription ${subscription.id}: ${e.message}")
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
  ): Long {
    return subscription.sources.map {
      scrapeSource(corrId, it)
    }.sumOf { it }

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

  private fun scrapeSource(corrId: String, source: ScrapeSourceEntity): Long {
    val stream = source.subscription!!.bucket!!.streamId;
    return scrapeService.scrape(corrId, source.scrapeRequest)
      .flatMapMany { scrapeResponse -> Flux.fromIterable(scrapeResponse.elements) }
      .map { importElement(corrId, it, stream) }
      .count()
      .blockOptional()
      .orElse(0)
  }

  private fun importElement(corrId: String, element: ScrapedElement, stream: UUID) {
    element.image?.let {
      importImageElement(corrId, it, stream)
    }
    element.selector?.let {
      importSelectorElement(corrId, it, stream)
    }
  }

  private fun importSelectorElement(corrId: String, scrapedData: ScrapedBySelector, stream: UUID) {
    log.info("[$corrId] importSelectorElement")
    scrapedData.fields.forEach {
      when (it.name) {
        "feed" -> importFeed(corrId, stream, JsonUtil.gson.fromJson(it.value.one.data, RemoteNativeFeed::class.java))
        else -> throw RuntimeException("Cannot handle field ${it.name}")
      }
    }
  }

  private fun importFeed(corrId: String, stream: UUID, feed: RemoteNativeFeed) {
    feed.items.map { createOrUpdate(corrId, it, stream, webDocumentDAO.findByUrlInStream(it.url, stream)) }
  }

  private fun createOrUpdate(corrId: String, item: WebDocument, stream: UUID, existing: WebDocumentEntity?) {
    existing?.let {
      it.updatedAt = Date()
      webDocumentDAO.save(it)
    } ?: run {
      meterRegistry.counter(AppMetrics.createWebDocument).increment()
      webDocumentDAO.save(item.asEntity(stream))
      log.info("[$corrId] created item ${item.url}")
    }
  }

  private fun importImageElement(corrId: String, scrapedData: ScrapedByBoundingBox, bucket: UUID) {
    log.info("[${corrId}] importImageElement")
    val id = CryptUtil.sha1(scrapedData.data.base64Data)
    if (!webDocumentDAO.existsById(UUID.fromString(id))) {
      log.info("[$corrId] created item $id")
//      webDocumentDAO.save(entity)
    }
  }
}

private fun WebDocument.asEntity(stream: UUID): WebDocumentEntity {
  val e = WebDocumentEntity()
  e.title = this.title
  e.streamId = stream
  e.contentRaw = this.contentRaw
  e.contentRawMime = this.contentRawMime
  e.contentText = this.contentText
  e.releasedAt = Date(this.publishedAt)
  e.updatedAt = this.updatedAt?.let { Date(this.updatedAt) } ?: e.releasedAt
  e.url = this.url
  return e
}
