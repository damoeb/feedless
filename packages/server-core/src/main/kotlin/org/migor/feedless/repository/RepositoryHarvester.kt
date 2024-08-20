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
import org.migor.feedless.plan.PlanConstraintsService
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
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

  @Autowired
  private lateinit var planConstraintsService: PlanConstraintsService

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  suspend fun handleRepository(corrId: String, repository: RepositoryEntity) {
    runCatching {
      log.info("[${corrId}] handleRepository ${repository.id}")

      meterRegistry.counter(
        AppMetrics.fetchRepository, listOf(
          Tag.of("type", "repository"),
          Tag.of("id", repository.id.toString()),
          Tag.of("format", "atom")
        )
      ).count()

      scrapeSources(corrId, repository)

      log.info("[$corrId] Harvesting done")
      documentService.applyRetentionStrategy(corrId, repository)
      updateScheduledNextAt(corrId, repository)
//        repositoryDAO.updateLastUpdatedAt(repository.id, Date())
    }.onFailure { log.error("[$corrId] handleRepository failed: ${it.message}", it) }
  }

//  fun updateNextHarvestDateAfterError(corrId: String, repository: RepositoryEntity, e: Throwable) {
//    log.info("[$corrId] handling ${e.message}")
////
//    val nextHarvestAt = if (e !is ResumableHarvestException) {
//      Date.from(Date().toInstant().plus(Duration.of(10, ChronoUnit.MINUTES)))
//    } else {
//      log.error("[$corrId] ${e.message}")
//      Date.from(Date().toInstant().plus(Duration.of(7, ChronoUnit.DAYS)))
//    }
//    repository.triggerScheduledNextAt = nextHarvestAt
//    repositoryDAO.save(repository)
//  }

  private val iso8601DateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.GERMANY)

  private fun updateScheduledNextAt(corrId: String, repository: RepositoryEntity) {
    val scheduledNextAt = repositoryService.calculateScheduledNextAt(
      repository.sourcesSyncCron,
      repository.ownerId,
      repository.product,
      LocalDateTime.now()
    )
    log.info("[$corrId] Next harvest scheduled for ${iso8601DateFormat.format(scheduledNextAt)}")
    repositoryDAO.updateScheduledNextAt(repository.id, scheduledNextAt)
  }

  private fun scrapeSources(
    corrId: String,
    repository: RepositoryEntity
  ): List<Any> {
    log.info("[$corrId] scrape ${repository.sources.size} sources")
    return repository.sources.map { source ->
      run {
        val subCorrId = newCorrId(parentCorrId = corrId)
        try {
          scrapeSource(subCorrId, source)
        } catch (e: Exception) {
          handleScrapeException(subCorrId, e, source)
        }
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

  private fun handleScrapeException(corrId: String, e: Throwable?, source: SourceEntity): Boolean {
    log.warn("[$corrId] scrape failed with ${e?.message}")
    if (e !is ResumableHarvestException) {
      meterRegistry.counter(AppMetrics.sourceHarvestError).increment()
//            notificationService.createNotification(corrId, repository.ownerId, e.message)
      sourceDAO.setErrorState(source.id, true, e?.message)
    }
    return true
  }

  fun scrapeSource(corrId: String, source: SourceEntity) {
    importElement(corrId, scrapeService.scrape(corrId, source), source.repositoryId, source)
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
    val repository = repositoryDAO.findById(repositoryId).orElseThrow()
    log.info("[$corrId] importFeed with ${feed.items.size} items with ${repository.plugins.size} plugins")

    if (feed.items.isEmpty()) {
      notificationService.createNotification(corrId, repository.ownerId, "Feed is empty", repository = repository, source = source)
    }
//  todo set name  sourceDAO.save()

    val maxItems = planConstraintsService.coerceRetentionMaxCapacity(null, repository.ownerId, repository.product)

    val documents = feed.items
      .distinctBy { it.url }
      .filterIndexed { index, _ -> maxItems?.let { it > index } ?: true }
      .mapNotNull {
        try {
          val existing = documentDAO.findByUrlAndRepositoryId(it.url, repositoryId)
          val updated = it.asEntity(repository.id, ReleaseStatus.released, source)
          updated.imageUrl = detectMainImageUrl(corrId, updated.contentHtml)
          createOrUpdate(corrId, updated, existing, repository)
        } catch (e: Exception) {
          log.error("[$corrId] importFeed failed: ${e.message}")
          null
        }
      }

    documentDAO.saveAll(documents.map { (_, document) -> document })
    documentPipelineJobDAO.saveAll(
      documents
        .filter { (new, _) -> new }
        .mapNotNull { (_, document) -> document }
        .flatMap { repository.plugins
          .mapIndexed { index, pluginRef -> toPipelineJob(pluginRef, it, index) }
          .toMutableList()
        }
    )

    val hasNew = documents.any { (new, _) -> new }
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
  ): Pair<Boolean, DocumentEntity?>? {
    return try {
      existing
        ?.let {
          existing.contentTitle = document.contentTitle
          existing.latLon = document.latLon
          existing.tags = document.tags
          existing.startingAt = document.startingAt
          log.debug("[$corrId] updated item ${document.url}")
          Pair(false, existing)
        }
        ?: run {
          meterRegistry.counter(AppMetrics.createDocument).increment()

          document.status = if (repository.plugins.isEmpty()) {
            ReleaseStatus.released
          } else {
            ReleaseStatus.unreleased
          }

          log.debug("[$corrId] saved ${repository.id} ${document.status} ${document.url}")

          Pair(true, document)
        }
    } catch (e: Exception) {
      if (e is ResumableHarvestException) {
        log.debug("[$corrId] ${e.message}")
      } else {
        log.error("[$corrId] createOrUpdate failed: ${e.message}")
        if (log.isDebugEnabled) {
          e.printStackTrace()
        }
      }
      null
    }
  }

  private fun toPipelineJob(plugin: PluginExecution, document: DocumentEntity, index: Int): DocumentPipelineJobEntity {
    val job = DocumentPipelineJobEntity()
    job.sequenceId = index
    job.documentId = document.id
    job.executorId = plugin.id
    job.executorParams = plugin.params
    return job
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
