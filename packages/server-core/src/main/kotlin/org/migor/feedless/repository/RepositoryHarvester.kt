package org.migor.feedless.repository

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.validation.Validation
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.apache.tika.Tika
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentEntity.Companion.LEN_URL
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.toPoint
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.pipeline.DocumentPipelineJobDAO
import org.migor.feedless.pipeline.DocumentPipelineJobEntity
import org.migor.feedless.pipeline.FeedlessPlugin
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.SourcePipelineJobDAO
import org.migor.feedless.pipeline.SourcePipelineJobEntity
import org.migor.feedless.pipeline.plugins.images
import org.migor.feedless.service.ScrapeOutput
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.toDto
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.web.WebExtractService.Companion.MIME_URL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
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

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun handleRepository(corrId: String, repositoryId: UUID) {
//    runBlocking {
      runCatching {
        log.debug("[${corrId}] handleRepository ${repositoryId}")

        meterRegistry.counter(
          AppMetrics.fetchRepository, listOf(
            Tag.of("type", "repository"),
            Tag.of("id", repositoryId.toString()),
            Tag.of("format", "atom")
          )
        ).count()

        val repository: RepositoryEntity = repositoryDAO.findById(repositoryId).orElseThrow()

        val appendCount = scrapeSources(corrId, repository)

        log.debug("[$corrId] appended items $appendCount to repo ${repository.id}")
        documentService.applyRetentionStrategy(corrId, repository)
        updateScheduledNextAt(corrId, repository)
        repositoryDAO.updateLastUpdatedAt(repository.id, Date())
      }.onFailure {
        log.error("[$corrId] handleRepository failed: ${it.message}", it)
      }
//    }
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
    log.debug("[$corrId] Next harvest scheduled for ${iso8601DateFormat.format(scheduledNextAt)}")
    repositoryDAO.updateScheduledNextAt(repository.id, scheduledNextAt)
  }

  private fun scrapeSources(
    corrId: String,
    repository: RepositoryEntity
  ): Int {
    log.debug("[$corrId] scrape ${repository.sources.size} sources")
    return repository.sources
      .filter { !it.erroneous }
      .fold(0) { agg, source ->
        try {
          val subCorrId = newCorrId(parentCorrId = corrId)
          agg + scrapeSource(subCorrId, source)
        } catch (e: Throwable) {
          handleScrapeException(corrId, e, source)
          agg
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

  private fun handleScrapeException(corrId: String, e: Throwable?, source: SourceEntity) {
    if (e !is ResumableHarvestException) {
      log.debug("[$corrId] disabling source")
      meterRegistry.counter(AppMetrics.sourceHarvestError).increment()
//            notificationService.createNotification(corrId, repository.ownerId, e.message)
      sourceDAO.setErrorState(source.id, true, e?.message)
    }
  }

  fun scrapeSource(corrId: String, source: SourceEntity): Int {
    return runBlocking {
      val output = scrapeService.scrape(corrId, source)
      importElement(corrId, output, source.repositoryId, source)
    }
  }

  private fun importElement(corrId: String, output: ScrapeOutput, repositoryId: UUID, source: SourceEntity): Int {
    log.debug("[$corrId] importElement")
    val lastAction = output.outputs.last()
    return lastAction.fragment?.let { fragment ->
      fragment.items?.let { importItems(corrId, repositoryId, it, fragment.fragments?.filter { it.data?.mimeType == MIME_URL }?.mapNotNull { it.data?.data }, source) }
    } ?: 0
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

  private fun importItems(corrId: String, repositoryId: UUID, items: List<JsonItem>, next: List<String>?, source: SourceEntity): Int {
    val repository = repositoryDAO.findById(repositoryId).orElseThrow()
    if (repository.plugins.isEmpty()) {
      log.debug("[$corrId] importItems size=${items.size}")
    } else {
      log.debug("[$corrId] importItems size=${items.size} with ${repository.plugins.size} plugins")
    }

    val start = Instant.now()
    val documents = items
      .distinctBy { it.url }
      .filterIndexed { index, _ -> index < 300 }
      .mapNotNull {
        try {
          val existing = documentDAO.findByUrlAndRepositoryId(it.url, repositoryId)
          val updated = it.asEntity(repository.id, ReleaseStatus.released, source)
          updated.imageUrl = detectMainImageUrl(corrId, updated.contentHtml)
          createOrUpdate(corrId, updated, existing, repository)
        } catch (e: Exception) {
          log.error("[$corrId] importItems failed: ${e.message}", e)
          null
        }
      }

    val validator = Validation.buildDefaultValidatorFactory().validator
    documentDAO.saveAll(documents
      .filter { document ->
        validator.validate(document).let { validation ->
          if (validation.isEmpty()) {
            true
          } else {
            log.warn("[$corrId] document ${StringUtils.substring(document.second.url, 100)} invalid: $validation")
            false
          }
        }
      }
      .map { (_, document) -> document })
    documentPipelineJobDAO.saveAll(
      documents
        .filter { (new, _) -> new }
        .map { (_, document) -> document }
        .flatMap { repository.plugins
          .mapIndexed { index, pluginRef -> toPipelineJob(pluginRef, it, index) }
          .toMutableList()
        }
    )

    log.debug("[$corrId] import took ${Duration.between(start, Instant.now()).toMillis()}")
    val hasNew = documents.any { (new, _) -> new }
    if (next?.isNotEmpty() == true) {
      if (hasNew) {
        val pageUrls = next.filterNot { url -> sourcePipelineJobDAO.existsBySourceIdAndUrl(source.id, url) }
        log.info("[$corrId] Trigger following ${pageUrls.size} (${next.size}) page urls ${pageUrls.joinToString(", ")}")
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
    return items.size
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
        log.debug("[$corrId] during detectMainImageUrl: ${e.message}")
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
  ): Pair<Boolean, DocumentEntity>? {
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
        log.error("[$corrId] createOrUpdate failed: ${e.message}", e)
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

private fun JsonItem.asEntity(repositoryId: UUID, status: ReleaseStatus, source: SourceEntity): DocumentEntity {
  val d = DocumentEntity()
  d.contentTitle = title
  d.sourceId = source.id
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
  d.latLon = source.latLon ?: this.latLng?.toPoint()
  d.contentHtml = contentHtml
  d.imageUrl = ""
  d.contentText = StringUtils.trimToEmpty(contentText)
  d.status = status
  d.attachments = attachments.map { it.toAttachment(d) }.toMutableList()
  d.publishedAt = publishedAt
  startingAt?.let {
    d.startingAt = startingAt
  }
//  d.updatedAt = updatedAt?.let { Date(updatedAt) } ?: d.publishedAt
  d.url = url
  if (url.length > LEN_URL) {
    throw IllegalArgumentException("url too long. max $LEN_URL, actual ${url.length}")
  }

  return d
}

private fun JsonAttachment.toAttachment(document: DocumentEntity): AttachmentEntity {
  val a = AttachmentEntity()
  a.contentType = type
  a.remoteDataUrl = url
  a.originalUrl = url
  a.size = length
  a.duration = duration
  a.documentId = document.id
  return a
}

fun nextCronDate(cronString: String, from: LocalDateTime): Date {
  return Date.from(CronExpression.parse(cronString).next(from)!!.toInstant(ZoneOffset.UTC))
}
