package org.migor.feedless.repository

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.apache.commons.lang3.StringUtils
import org.apache.tika.Tika
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.SourceEntity
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.data.jpa.repositories.SourceDAO
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.toPoint
import org.migor.feedless.generated.types.Enclosure
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.notification.NotificationService
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.DocumentPipelineJobEntity
import org.migor.feedless.pipeline.FeedlessPlugin
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobEntity
import org.migor.feedless.pipeline.plugins.images
import org.migor.feedless.service.ScrapeOutput
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.scrape}")
class RepositoryHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(RepositoryHarvester::class.simpleName)

  @Autowired
  private lateinit var scrapeService: ScrapeService

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var documentPipelineJobDAO: DocumentPipelineJobDAO

  @Autowired
  private lateinit var sourcePipelineJobDAO: SourcePipelineJobDAO

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var meterRegistry: MeterRegistry

  @Autowired
  private lateinit var sourceDAO: SourceDAO

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Autowired
  private lateinit var notificationService: NotificationService

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun handleRepository(corrId: String, repository: RepositoryEntity) {
    log.info("[${corrId}] handleRepository ${repository.id}")

    meterRegistry.counter(
      AppMetrics.fetchRepository, listOf(
        Tag.of("type", "repository"),
        Tag.of("id", repository.id.toString()),
        Tag.of("format", "atom")
      )
    ).count()

    try {
      scrapeSources(corrId, repository)
        .blockLast()

      documentService.applyRetentionStrategy(corrId, repository)
      log.info("[$corrId] Harvesting done")
      updateScheduledNextAt(corrId, repository)
      repositoryDAO.updateLastUpdatedAt(repository.id, Date())

    } catch (it: Exception) {
      log.error(it.message)
      notificationService.createNotification(corrId, repository.ownerId, it.message ?: "", repository = repository)
//      if (log.isDebugEnabled) {
//        it.printStackTrace()
//      }
      updateNextHarvestDateAfterError(corrId, repository, it)
    }
  }

  fun updateNextHarvestDateAfterError(corrId: String, repository: RepositoryEntity, e: Throwable) {
    log.info("[$corrId] handling ${e.message}")
//
    val nextHarvestAt = if (e !is ResumableHarvestException) {
      Date.from(Date().toInstant().plus(Duration.of(10, ChronoUnit.MINUTES)))
    } else {
      log.error("[$corrId] ${e.message}")
      Date.from(Date().toInstant().plus(Duration.of(7, ChronoUnit.DAYS)))
    }
    repository.triggerScheduledNextAt = nextHarvestAt
    repositoryDAO.save(repository)
  }

  private fun updateScheduledNextAt(corrId: String, repository: RepositoryEntity) {
    val scheduledNextAt = repositoryService.calculateScheduledNextAt(
      repository.sourcesSyncCron,
      repository.ownerId,
      repository.product,
      LocalDateTime.now()
    )
    log.info("[$corrId] Next harvest scheduled for $scheduledNextAt")
    repositoryDAO.updateScheduledNextAt(repository.id, scheduledNextAt)
  }

  private fun scrapeSources(
    corrId: String,
    repository: RepositoryEntity
  ): Flux<Unit> {
    log.info("[$corrId] scrape ${repository.sources.size} sources")
    return Flux.fromIterable(repository.sources)
      .flatMap { source ->
        val subCorrId = newCorrId(parentCorrId = corrId)
        try {
          scrapeSource(corrId, source)
            .retryWhen(Retry.fixedDelay(3, Duration.ofMinutes(3)))
            .also { recoverErrorState(source) }
        } catch (e: Exception) {
          log.warn("[$subCorrId] ${e.message}")
          if (e !is ResumableHarvestException) {
            meterRegistry.counter(AppMetrics.sourceHarvestError).increment()
//            notificationService.createNotification(corrId, repository.ownerId, e.message)
            sourceDAO.setErrorState(source.id, true, e.message)
          }
          Flux.empty()
        }
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

  private fun recoverErrorState(source: SourceEntity) {
    if (source.erroneous) {
      sourceDAO.setErrorState(source.id, false, null)
    }
  }

  fun scrapeSource(corrId: String, source: SourceEntity): Mono<Unit> {
    return scrapeService.scrape(corrId, source)
      .map { importElement(corrId, it, source.repositoryId, source) }
  }

  private fun importElement(corrId: String, output: ScrapeOutput, repositoryId: UUID, source: SourceEntity) {
    log.debug("[$corrId] importElement")
    val lastAction = output.outputs.last()
    lastAction.execute?.let {
      it.data.org_feedless_feed?.let { importFeed(corrId, repositoryId, it, source) }
    } ?: lastAction.extract?.let {
      TODO()
    } ?: lastAction.fetch?.let {
      TODO()
    }
//    lastAction.extract.image?.let {
//      importImageElement(corrId, it, repositoryId, source)
//    }
//    output.selector?.let {
//      importSelectorElement(corrId, it, repositoryId, source)
//    }
  }

//  private fun importSelectorElement(
//    corrId: String,
//    scrapedData: ScrapedBySelector,
//    repositoryId: UUID,
//    source: SourceEntity
//  ) {
//    log.debug("[$corrId] importSelectorElement")
//    scrapedData.fields?.let { fields ->
//      fields.forEach {
//        when (it.name) {
//          FeedlessPlugins.org_feedless_feed.name -> importFeed(
//            corrId,
//            repositoryId,
//            JsonUtil.gson.fromJson(it.value.one.data, RemoteNativeFeed::class.java),
//            source
//          )
//
//          else -> throw BadRequestException("Cannot handle field '${it.name}' ($corrId)")
//        }
//      }
//    } ?: importScrapedData(corrId, scrapedData, repositoryId, source)
//  }

//  private fun importScrapedData(
//    corrId: String,
//    scrapedData: ScrapedBySelector,
//    repositoryId: UUID,
//    source: SourceEntity
//  ) {
//    log.info("[$corrId] importScrapedData")
//    val document = scrapedData.asEntity(repositoryId, source.tags)
//
//    val repository = repositoryDAO.findById(repositoryId).orElseThrow()
//
//    createOrUpdate(
//      corrId,
//      document,
//      documentDAO.findByUrlAndRepositoryId(document.url, repositoryId),
//      repository
//    )
//  }

  private fun importFeed(corrId: String, repositoryId: UUID, feed: RemoteNativeFeed, source: SourceEntity) {
    log.info("[$corrId] importFeed")
    val repository = repositoryDAO.findById(repositoryId).orElseThrow()

    if (feed.items.isEmpty()) {
      notificationService.createNotification(corrId, repository.ownerId, "Feed is empty", repository = repository, source = source)
    }
//  todo set name  sourceDAO.save()

    val hasNew = feed.items
      .distinctBy { it.url }
      .fold(false) { acc, it -> try {
        val existing = documentDAO.findByUrlAndRepositoryId(it.url, repositoryId)
        val updated = it.asEntity(repository.id, ReleaseStatus.released, source)
        updated.imageUrl = detectMainImageUrl(corrId, updated.contentHtml)
        createOrUpdate(corrId, updated, existing, repository)
        acc || existing == null
      } catch (e: Exception) {
        log.error("[$corrId] ${e.message}")
        acc
      }
      }

    if (feed.nextPageUrls?.isNotEmpty() == true) {
      if (hasNew) {
        val pageUrls = feed.nextPageUrls.filterNot { url -> sourcePipelineJobDAO.existsBySourceIdAndUrl(source.id, url) }
        log.info("[$corrId] Trigger following ${pageUrls.size} (${feed.nextPageUrls.size}) page urls ${pageUrls.joinToString(", ")}")
        sourcePipelineJobDAO.saveAll(
          pageUrls
          .mapIndexed { index, url ->
          run {
            val e = SourcePipelineJobEntity()
            e.sourceId = source.id
            e.url = url
            e.sequenceId = index
            e
          }
        })
      } else {
        log.info("[$corrId] wont follow page urls")
      }
    }
  }

  fun detectMainImageUrl(corrId: String, contentHtml: String?): String? {
    return contentHtml?.let {
      Jsoup.parse(contentHtml).images()
        .sortedByDescending { calculateSize(corrId, it) }
        .map { it.attr("src") }
        .firstOrNull()
    }
  }

  private fun calculateSize(corrId: String, el: Element): Int {
    return if (el.hasAttr("width") && el.hasAttr("height")) {
      try {
        el.attr("width").toInt() * el.attr("height").toInt()
      } catch (e: Exception) {
        log.warn("[$corrId] during detectMainImageUrl: ${e.message}")
        400
      }
    } else {
      0
    }
  }

  private fun createOrUpdate(
    corrId: String,
    document: DocumentEntity,
    existing: DocumentEntity?,
    repository: RepositoryEntity
  ) {
    try {
      existing
        ?.let {
          existing.contentTitle = document.contentTitle
          existing.latLon = document.latLon
          existing.tags = document.tags
          existing.startingAt = document.startingAt
          documentDAO.save(existing)
          log.debug("[$corrId] updated item ${document.url}")
        }
        ?: run {
          meterRegistry.counter(AppMetrics.createDocument).increment()

          document.status = if (repository.plugins.isEmpty()) {
            ReleaseStatus.released
          } else {
            ReleaseStatus.unreleased
          }
          documentDAO.save(document)

          document.plugins = repository.plugins
            .mapIndexed { index, pluginRef -> toPipelineJob(pluginRef, document, index) }
            .toMutableList()

          log.info("[$corrId] saved ${repository.id} ${document.status} ${document.url} with ${document.plugins.size} plugins")
        }
    } catch (e: Exception) {
      if (e is ResumableHarvestException) {
        log.debug("[$corrId] ${e.message}")
      } else {
        log.error("[$corrId] ${e.message}")
        if (log.isDebugEnabled) {
          e.printStackTrace()
        }
      }
    }
  }

  private fun toPipelineJob(plugin: PluginExecution, document: DocumentEntity, index: Int): DocumentPipelineJobEntity {
    val job = DocumentPipelineJobEntity()
    job.sequenceId = index
    job.documentId = document.id
    job.executorId = plugin.id
    job.executorParams = plugin.params
    return documentPipelineJobDAO.save(job)
  }

//  private fun importImageElement(
//    corrId: String,
//    scrapedData: ScrapedByBoundingBox,
//    repositoryId: UUID,
//    source: SourceEntity
//  ) {
//    log.info("[${corrId}] importImageElement")
//    val id = CryptUtil.sha1(scrapedData.data.base64Data)
//    if (!documentDAO.existsByContentTitleAndRepositoryId(id, repositoryId)) {
//      log.info("[$corrId] create item $id")
//      TODO("not implemented")
////      webDocumentDAO.save(entity)
//    }
//  }
}

fun SourceEntity.toScrapeRequest(corrId: String): ScrapeRequest {
  return toDto(corrId)
}

inline fun <reified T : FeedlessPlugin> List<PluginExecution>.mapToPluginInstance(pluginService: PluginService): List<Pair<T, PluginExecutionParamsInput>> {
  return this.map { Pair(pluginService.resolveById<T>(it.id), it.params) }
    .mapNotNull { (plugin, params) ->
      if (plugin == null) {
        null
      } else {
        Pair(plugin, params)
      }
    }
}

private fun WebDocument.asEntity(repositoryId: UUID, status: ReleaseStatus, source: SourceEntity): DocumentEntity {
  val d = DocumentEntity()
  d.contentTitle = contentTitle
  d.repositoryId = repositoryId
  if (StringUtils.isNotBlank(contentRawBase64)) {
    val tika = Tika()
    val contentRawBytes = contentRawBase64!!.toByteArray()
    val mime = tika.detect(contentRawBytes)
    d.contentRaw = if (mime.startsWith("text/")) {
      contentRawBytes
    } else {
      Base64.getDecoder().decode(contentRawBase64)
    }
    d.contentRawMime = mime
  }
  d.tags = source.tags
  d.latLon = source.latLon ?: this.localized?.toPoint()
  d.contentHtml = contentHtml
  d.imageUrl = ""
  d.contentText = StringUtils.trimToEmpty(contentText)
  d.status = status
  enclosures?.let {
    d.attachments = it.map { it.toAttachment(d) }.toMutableList()
  }
  d.publishedAt = Date(publishedAt)
  startingAt?.let {
    d.startingAt = Date(startingAt)
  }
  d.updatedAt = updatedAt?.let { Date(updatedAt) } ?: d.publishedAt
  d.url = url
  return d
}

private fun Enclosure.toAttachment(document: DocumentEntity): AttachmentEntity {
  val a = AttachmentEntity()
  a.contentType = type
  a.remoteDataUrl = url
  a.originalUrl = url
  a.size = size
  a.duration = duration
  a.documentId = document.id
  return a
}

fun nextCronDate(cronString: String, from: LocalDateTime): Date {
  return Date.from(CronExpression.parse(cronString).next(from)!!.toInstant(ZoneOffset.UTC))
}
