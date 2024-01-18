package org.migor.feedless.harvest

import io.micrometer.core.instrument.MeterRegistry
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.PluginRef
import org.migor.feedless.data.jpa.models.ScrapeSourceEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapedByBoundingBox
import org.migor.feedless.generated.types.ScrapedBySelector
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.service.PluginService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.RetentionStrategyService
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
  lateinit var retentionStrategyService: RetentionStrategyService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Autowired
  lateinit var pluginService: PluginService

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun handleSourceSubscription(corrId: String, subscription: SourceSubscriptionEntity) {
    log.info("[${corrId}] handleSourceSubscription ${subscription.id}")
    try {
      updateScheduledNextAt(corrId, subscription)
      if (subscription.triggerScheduledNextAt == null) {
        log.info("[$corrId] is unscheduled yet")
      } else {
        scrapeSources(corrId, subscription)
        log.info("[$corrId] Harvesting done")

        sourceSubscriptionDAO.setLastUpdatedAt(subscription.id, Date())
      }

    } catch (e: Exception) {
      log.error("[$corrId] Cannot update scheduled subscription ${subscription.id}: ${e.message}")
    }
  }

  private fun updateScheduledNextAt(corrId: String, subscription: SourceSubscriptionEntity) {
    val scheduledNextAt = nextCronDate(subscription.schedulerExpression)
    log.info("[$corrId] Next import scheduled for $scheduledNextAt")
    sourceSubscriptionDAO.setScheduledNextAt(subscription.id, scheduledNextAt)
  }

  private fun scrapeSources(
    corrId: String,
    subscription: SourceSubscriptionEntity
  ) {
    subscription.sources.map {
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

    retentionStrategyService.applyRetentionStrategy(corrId, subscription)
  }

  private fun scrapeSource(corrId: String, source: ScrapeSourceEntity) {
    val subscriptionId = source.subscription!!.id
    scrapeService.scrape(corrId, source.scrapeRequest)
      .flatMapMany { scrapeResponse -> Flux.fromIterable(scrapeResponse.elements) }
      .map { importElement(corrId, it, subscriptionId) }
      .blockLast()
  }

  private fun importElement(corrId: String, element: ScrapedElement, subscriptionId: UUID) {
    log.info("[$corrId] importElement")
    element.image?.let {
      importImageElement(corrId, it, subscriptionId)
    }
    element.selector?.let {
      importSelectorElement(corrId, it, subscriptionId)
    }
  }

  private fun importSelectorElement(corrId: String, scrapedData: ScrapedBySelector, subscriptionId: UUID) {
    log.info("[$corrId] importSelectorElement")
    scrapedData.fields?.let { fields ->
      fields.forEach {
        when (it.name) {
          FeedlessPlugins.org_feedless_feed.name -> importFeed(
            corrId,
            subscriptionId,
            JsonUtil.gson.fromJson(it.value.one.data, RemoteNativeFeed::class.java)
          )

          else -> throw RuntimeException("Cannot handle field ${it.name}")
        }
      }
    }
    importScrapedData(corrId, scrapedData, subscriptionId)
  }

  private fun importScrapedData(corrId: String, scrapedData: ScrapedBySelector, subscriptionId: UUID) {
    val webDocument = scrapedData.asEntity(subscriptionId)


    val subscription = sourceSubscriptionDAO.findById(subscriptionId).orElseThrow()
    createOrUpdate(
      corrId,
      webDocument,
      webDocumentDAO.findByUrlAndSubscriptionId(webDocument.url, subscriptionId),
      subscription.plugins
    )
  }

  private fun importFeed(corrId: String, subscriptionId: UUID, feed: RemoteNativeFeed) {
    val subscription = sourceSubscriptionDAO.findById(subscriptionId).orElseThrow()
//    subscription.scoped
    feed.items.map {
      val existing = webDocumentDAO.findByUrlAndSubscriptionId(it.url, subscriptionId)
      val updated = it.asEntity(subscription.id, ReleaseStatus.released)
      createOrUpdate(corrId, updated, existing, subscription.plugins)
    }
  }

  private fun createOrUpdate(
    corrId: String,
    webDocument: WebDocumentEntity,
    existing: WebDocumentEntity?,
    plugins: List<PluginRef>
  ) {
    existing?.let {
      it.updatedAt = Date()
      webDocumentDAO.save(it)
    } ?: run {
      meterRegistry.counter(AppMetrics.createWebDocument).increment()

      plugins.forEach { pluginService.resolveEntityTransformerById(it.id)
        ?.mapEntity(corrId, webDocument, it.params) }
      webDocumentDAO.save(webDocument)
      log.info("[$corrId] created item ${webDocument.url}")
    }
  }

  private fun importImageElement(corrId: String, scrapedData: ScrapedByBoundingBox, subscriptionId: UUID) {
    log.info("[${corrId}] importImageElement")
    val id = CryptUtil.sha1(scrapedData.data.base64Data)
    if (!webDocumentDAO.existsByContentTitleAndSubscriptionId(id, subscriptionId)) {
      log.info("[$corrId] created item $id")
      // todo hier
//      webDocumentDAO.save(entity)
    }
  }
}

private fun ScrapedBySelector.asEntity(subscriptionId: UUID): WebDocumentEntity {
  val e = WebDocumentEntity()
  e.subscriptionId = subscriptionId
  pixel?.let {
    e.contentTitle = CryptUtil.sha1(it.base64Data)
    e.contentRaw = it.base64Data
    e.contentRawMime = "image/png"
  } ?: {
    html?.let {
      e.contentTitle = CryptUtil.sha1(it.data)
      e.contentHtml = it.data
    }
  }

  e.contentText = text.data
  e.status = ReleaseStatus.released
  e.releasedAt = Date()
  e.updatedAt = Date()
  e.url = "https://feedless.org/d/${e.id}"
  return e
}

private fun WebDocument.asEntity(subscriptionId: UUID, status: ReleaseStatus): WebDocumentEntity {
  val e = WebDocumentEntity()
  e.contentTitle = contentTitle
  e.subscriptionId = subscriptionId
  e.contentRaw = contentRaw
  e.contentRawMime = contentRawMime
  e.contentText = contentText
  e.status = status
  e.releasedAt = Date(publishedAt)
  e.updatedAt = updatedAt?.let { Date(updatedAt) } ?: e.releasedAt
  e.url = url
  return e
}

fun nextCronDate(cronString: String): Date {
  return Date.from(CronExpression.parse(cronString).next(LocalDateTime.now())!!.toInstant(ZoneOffset.UTC))
}
