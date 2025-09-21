package org.migor.feedless.repository

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import jakarta.annotation.PostConstruct
import jakarta.validation.Validation
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.apache.tika.Tika
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.NoItemsRetrievedException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.DocumentEntity.Companion.LEN_URL
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.toPoint
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractFragmentPart
import org.migor.feedless.pipeline.DocumentPipelineJobEntity
import org.migor.feedless.pipeline.DocumentPipelineService
import org.migor.feedless.pipeline.SourcePipelineJobEntity
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.pipeline.plugins.images
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebExtractService.Companion.MIME_URL
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.ConnectException
import java.net.UnknownHostException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.repository} & ${AppLayer.service} & ${AppLayer.scheduler}")
class RepositoryHarvester(
  private val documentService: DocumentService,
  private val documentPipelineService: DocumentPipelineService,
  private val sourcePipelineService: SourcePipelineService,
  private val sourceService: SourceService,
  private val scrapeService: ScrapeService,
  private val meterRegistry: MeterRegistry,
  private val repositoryService: RepositoryService,
  private val harvestService: HarvestService,
) {

  private val log = LoggerFactory.getLogger(RepositoryHarvester::class.simpleName)
  private val iso8601DateFormat: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  private lateinit var harvestOffsetTimer: Timer

  @PostConstruct
  fun register() {
    harvestOffsetTimer = Timer
      .builder("harvest.offset")
      .description("offset between when harvest should and did happen")
      .register(meterRegistry)
  }

  suspend fun handleRepository(repositoryId: RepositoryId) {
    val corrId = coroutineContext.corrId()
    runCatching {
      log.info("[${corrId}] handleRepository $repositoryId")

      meterRegistry.counter(
        AppMetrics.fetchRepository, listOf(
          Tag.of("type", "repository"),
          Tag.of("id", repositoryId.toString()),
        )
      ).count()

      val repository = repositoryService.findById(repositoryId).orElseThrow()

      repository.triggerScheduledNextAt?.let {
        val diffInMillis = Duration.ofMillis(ChronoUnit.MILLIS.between(LocalDateTime.now(), it))
        harvestOffsetTimer.record(diffInMillis)
      }

      scrapeSources(repositoryId)

      val scheduledNextAt = repositoryService.calculateScheduledNextAt(
        repository.sourcesSyncCron,
        UserId(repository.ownerId),
        repository.product,
        LocalDateTime.now()
      )
      log.debug("[$corrId] Next harvest at ${scheduledNextAt.format(iso8601DateFormat)}")
      repository.triggerScheduledNextAt = scheduledNextAt
      repository.lastUpdatedAt = LocalDateTime.now()
      repositoryService.save(repository)

    }.onFailure {
      log.error("[$corrId] handleRepository failed: ${it.message}", it)
    }
  }

  private suspend fun scrapeSources(
    repositoryId: RepositoryId,
  ) {
    val corrId = coroutineContext.corrId()
    var sources: List<SourceEntity>
    var currentPage = 0
    do {
      sources = sourceService.findAllByRepositoryIdFiltered(repositoryId, PageRequest.of(currentPage++, 5))
        .filter { !it.disabled }
        .distinctBy { it.id }
      log.info("[$corrId] queueing page $currentPage with ${sources.size} sources")

      sources
        .forEachIndexed { index, source ->
          run {
            val logCollector = LogCollector()
            val harvest = HarvestEntity()
            harvest.sourceId = source.id
            harvest.startedAt = LocalDateTime.now()

            try {
              log.info("[$corrId] scraping source $currentPage/$index ${source.id}")
              val retrieved = scrapeSource(source, logCollector)

              if (source.errorsInSuccession > 0) {
                source.errorsInSuccession = 0
              }
              source.lastErrorMessage = null
              source.lastRecordsRetrieved = retrieved
              source.lastRefreshedAt = LocalDateTime.now()
              sourceService.save(source)

            } catch (e: Throwable) {
              handleScrapeException(e, source, logCollector)
            } finally {

              harvest.finishedAt = LocalDateTime.now()
              harvest.logs = StringUtils.abbreviate(logCollector.logs.map {
                "${
                  it.time.toLocalDateTime().format(iso8601DateFormat)
                }  ${it.message}"
              }
                .joinToString("\n"), "...", 32000)
              harvestService.saveLast(harvest)
            }
          }
        }
    } while (sources.isNotEmpty())

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
//    val articles = recordDAO.findAllThrottled(
//      importer.feedId,
//      importer.triggerScheduledLastAt ?: defaultScheduledLastAt,
//      pageable
//    )
//
//    refineAndImportArticlesScheduled(corrId, articles, importer)
  }

  private suspend fun handleScrapeException(
    e: Throwable?,
    source: SourceEntity,
    logCollector: LogCollector
  ) {
    val corrId = coroutineContext.corrId()
    log.error("[$corrId] scrape failed ${e?.message}")
    logCollector.log("[$corrId] scrape failed ${e?.message}")

    if (e !is ResumableHarvestException && e !is UnknownHostException && e !is ConnectException && e !is NoItemsRetrievedException) {
      logCollector.log("[$corrId] scrape error '${e?.message}'")
      meterRegistry.counter(AppMetrics.sourceHarvestError).increment()
//            notificationService.createNotification(corrId, repository.ownerId, e.message)
      source.lastRecordsRetrieved = 0
      source.lastRefreshedAt = LocalDateTime.now()

      val maxErrorCount = 3
      source.errorsInSuccession += 1
      logCollector.log("[$corrId] error count '${source.errorsInSuccession}'")
      log.info("source ${source.id} error '${e?.message}' increment -> '${source.errorsInSuccession}'")
      source.disabled = source.errorsInSuccession >= maxErrorCount
      source.lastErrorMessage = e?.message

      if (source.disabled) {
        logCollector.log("[$corrId] disabled source")
        log.info("source ${source.id} disabled")
      }
    } else {
      source.errorsInSuccession = 0
      source.lastErrorMessage = e.message
      source.lastRefreshedAt = LocalDateTime.now()
    }
    sourceService.save(source)
  }

  suspend fun scrapeSource(source: SourceEntity, logCollector: LogCollector): Int {
    val output = scrapeService.scrape(source, logCollector)
    return importElement(output, RepositoryId(source.repositoryId), source, logCollector)
  }

  private suspend fun importElement(
    output: ScrapeOutput,
    repositoryId: RepositoryId,
    source: SourceEntity,
    logCollector: LogCollector
  ): Int {
    val corrId = coroutineContext.corrId()
    log.debug("[$corrId] importElement")
    val repository = repositoryService.findById(repositoryId).orElseThrow()
    return if (output.outputs.isEmpty()) {
      throw NoItemsRetrievedException()
    } else {
      val lastAction = output.outputs.last()
      val documents = lastAction.fragment?.let { fragment ->
        if (fragment.items?.isEmpty() == false) {
          importItems(
            repository,
            fragment.items,
            fragment.fragments?.filter { it.data?.mimeType == MIME_URL }?.mapNotNull { it.data?.data },
            source,
            logCollector
          )
        } else {
          if (fragment.fragments?.isEmpty() == false) {
            fragment.fragments.flatMap { importFragment(repository, it, source, logCollector) }
          } else {
            emptyList()
          }
        }
      } ?: emptyList()

      triggerPostReleaseEffects(repository, documents)
      triggerPlugins(repository, documents)
      documents.size
    }
//    lastAction.extract.image?.let {
//      importImageElement(corrId, it, repositoryId, source)
//    }
//    output.selector?.let {
//      importSelectorElement(corrId, it, repositoryId, source)
//    }
  }

  private suspend fun triggerPostReleaseEffects(
    repository: RepositoryEntity,
    documents: List<Pair<Boolean, DocumentEntity>>
  ) {
    documentService.triggerPostReleaseEffects(documents.map { it.second }, repository)
  }

  private suspend fun triggerPlugins(
    repository: RepositoryEntity,
    documents: List<Pair<Boolean, DocumentEntity>>
  ) {
    val corrId = coroutineContext.corrId()!!
    if (repository.plugins.isNotEmpty()) {
      try {
        log.debug("[$corrId] delete all document job by documents")
        documentPipelineService.deleteAllByDocumentIdIn(
          documents.filter { (isNew, _) -> !isNew }
            .map { (_, document) -> document.id })
      } catch (e: Exception) {
        log.warn("[$corrId] deleteAllByDocumentIdIn failed: ${e.message}")
      }
    }
    documentPipelineService.saveAll(
      documents
        .map { (_, document) -> document }
        .flatMap {
          repository.plugins
            .mapIndexed { index, pluginRef -> toDocumentPipelineJob(pluginRef, it, index) }
            .toMutableList()
        }
    )
  }

  private suspend fun importFragment(
    repository: RepositoryEntity,
    fragment: ScrapeExtractFragment,
    source: SourceEntity,
    logCollector: LogCollector
  ): List<Pair<Boolean, DocumentEntity>> {
    val corrId = coroutineContext.corrId()!!

    log.info("[${corrId}] importImageElement")

    val updated = fragment.asEntity(repository.id, source)
    val existing =
      documentService.findFirstByContentHashOrUrlAndRepositoryId(
        updated.contentHash,
        updated.url,
        RepositoryId(repository.id)
      )

    val validNewOrUpdatedDocuments =
      filterInvalidDocuments(listOf(createOrUpdate(updated, existing, repository, logCollector)!!))
    documentService.saveAll(validNewOrUpdatedDocuments)

    return listOf(Pair(existing != null, updated))
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

  private suspend fun importItems(
    repository: RepositoryEntity,
    items: List<JsonItem>,
    next: List<String>?,
    source: SourceEntity,
    logCollector: LogCollector
  ): List<Pair<Boolean, DocumentEntity>> {
    val corrId = coroutineContext.corrId()!!
    if (items.isEmpty()) {
      throw NoItemsRetrievedException()
    }

    log.info("[$corrId] importItems size=${items.size}")
    if (repository.plugins.isEmpty()) {
      logCollector.log("[$corrId] importItems size=${items.size}")
    } else {
      logCollector.log("[$corrId] importItems size=${items.size} with [${repository.plugins.joinToString(", ") { it.id }}]")
    }

    val start = Instant.now()
    val newOrUpdatedDocuments = items
      .map { it.asEntity(repository.id, ReleaseStatus.released, source) }
      .distinctBy { it.contentHash }
      .filterIndexed { index, _ -> index < 300 }
      .mapNotNull { updated ->
        try {
          val existing =
            documentService.findFirstByContentHashOrUrlAndRepositoryId(
              updated.contentHash,
              updated.url,
              RepositoryId(repository.id)
            )
          updated.imageUrl = detectMainImageUrl(updated.html)
          createOrUpdate(updated, existing, repository, logCollector)
        } catch (e: Exception) {
          logCollector.log("[$corrId] importItems failed: ${e.message}")
          log.error("[$corrId] importItems failed: ${e.message}", e)
          null
        }
      }

    val validNewOrUpdatedDocuments = filterInvalidDocuments(newOrUpdatedDocuments)
    documentService.saveAll(validNewOrUpdatedDocuments)

    if (validNewOrUpdatedDocuments.isNotEmpty()) {
      log.info("[$corrId] ${repository.id}/${source.id} found ${validNewOrUpdatedDocuments.size} documents")
    }

    log.debug("[$corrId] import took ${Duration.between(start, Instant.now()).toMillis()}")
    logCollector.log("[$corrId] import took ${Duration.between(start, Instant.now()).toMillis()}")
    val hasNew = newOrUpdatedDocuments.any { (new, _) -> new }
    if (next?.isNotEmpty() == true) {
      if (hasNew) {
        val pageUrls = next.filterNot { url -> sourcePipelineService.existsBySourceIdAndUrl(SourceId(source.id), url) }
        log.info("[$corrId] Following ${next.size} pagination urls ${pageUrls.joinToString(", ")}")
        sourcePipelineService.saveAll(
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
        log.debug("[$corrId] wont follow page urls")
      }
    }
    return newOrUpdatedDocuments
  }

  private suspend fun filterInvalidDocuments(
    newOrUpdatedDocuments: List<Pair<Boolean, DocumentEntity>>,
  ): List<DocumentEntity> {
    val validator = Validation.buildDefaultValidatorFactory().validator
    val validNewOrUpdatedDocuments = newOrUpdatedDocuments
      .filter { document ->
        validator.validate(document).let { validation ->
          if (validation.isEmpty()) {
            true
          } else {
            val corrId = coroutineContext.corrId()!!
            log.warn("[$corrId] document ${StringUtils.substring(document.second.url, 100)} invalid: $validation")
            false
          }
        }
      }.map { (_, document) -> document }
    return validNewOrUpdatedDocuments
  }

  suspend fun detectMainImageUrl(html: String?): String? {
    return html?.let {
      Jsoup.parse(html).images()
        .sortedByDescending {
          runBlocking {
            calculateSize(it)
          }
        }
        .map { it.attr("src") }
        .firstOrNull()
    }
  }

  private suspend fun calculateSize(el: Element): Int {
    return if (el.hasAttr("width") && el.hasAttr("height")) {
      try {
        el.attr("width").toInt() * el.attr("height").toInt()
      } catch (e: Exception) {
        val corrId = coroutineContext.corrId()
        log.debug("[$corrId] during detectMainImageUrl: ${e.message}")
        400
      }
    } else {
      0
    }
  }

  private suspend fun createOrUpdate(
    document: DocumentEntity,
    existing: DocumentEntity?,
    repository: RepositoryEntity,
    logCollector: LogCollector
  ): Pair<Boolean, DocumentEntity>? {
    val corrId = coroutineContext.corrId()
    return try {
      if (existing == null) {
        meterRegistry.counter(AppMetrics.createDocument).increment()

        document.status = if (repository.plugins.isEmpty()) {
          logCollector.log("[$corrId] released ${document.url}")
          ReleaseStatus.released
        } else {
          logCollector.log("[$corrId] queued for post-processing ${document.url}")
          ReleaseStatus.unreleased
        }

        Pair(true, document)
      } else {
        if (repository.plugins.isEmpty()) {
          existing.title = document.title
          existing.text = document.text
          existing.contentHash = document.contentHash
          existing.latLon = document.latLon
          existing.tags = document.tags
          existing.startingAt = document.startingAt
          logCollector.log("[$corrId] updated item ${document.url}")
          Pair(false, existing)
        } else {
//          if (repository.lastUpdatedAt.isAfter(existing.createdAt)) {
//            existing.status = ReleaseStatus.unreleased
//            Pair(false, existing)
//          } else {
          null
//          }
        }
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

  private fun toDocumentPipelineJob(
    plugin: PluginExecution,
    document: DocumentEntity,
    index: Int
  ): DocumentPipelineJobEntity {
    val job = DocumentPipelineJobEntity()
    job.sequenceId = index
    job.documentId = document.id
    job.pluginId = plugin.id
    job.executorParams = plugin.params
    return job
  }
}

private fun ScrapeExtractFragment.asEntity(repositoryId: UUID, source: SourceEntity): DocumentEntity {
  val d = DocumentEntity()
  d.title = LocalDateTime.now().toString()
  d.sourceId = source.id
  d.repositoryId = repositoryId
  if (data?.data != null) {
    d.raw = Base64.getDecoder().decode(data.data)
    d.rawMimeType = data.mimeType
  }
  d.tags = source.tags
  d.contentHash = CryptUtil.sha1(
    when (uniqueBy) {
      ScrapeExtractFragmentPart.html -> html?.data
      ScrapeExtractFragmentPart.text -> text?.data
      ScrapeExtractFragmentPart.data -> data?.data
    }!!
  )
  d.html = html?.data
  d.imageUrl = ""
  d.text = StringUtils.trimToEmpty(text?.data)
  d.status = ReleaseStatus.released
  d.url = "https://does-not-exist"
  return d
}

//inline fun <reified T : FeedlessPlugin> List<PluginExecution>.mapToPluginInstance(pluginService: PluginService): List<Pair<T, PluginExecutionParamsInput>> {
//  return this.map { Pair(pluginService.resolveById<T>(it.id), it.params) }
//    .mapNotNull { (plugin, params) ->
//      if (plugin == null) {
//        null
//      } else {
//        Pair(plugin, params)
//      }
//    }
//}

private fun JsonItem.asEntity(repositoryId: UUID, status: ReleaseStatus, source: SourceEntity): DocumentEntity {
  val d = DocumentEntity()
  d.title = title
  d.sourceId = source.id
  d.repositoryId = repositoryId
  if (StringUtils.isNotBlank(rawBase64)) {
    val tika = Tika()
    val rawBytes = rawBase64!!.toByteArray()
    val mime = tika.detect(rawBytes)
    d.raw = if (mime.startsWith("text/")) {
      rawBytes
    } else {
      Base64.getDecoder().decode(rawBase64)
    }
    d.rawMimeType = mime
  }
  d.tags = source.tags
  d.contentHash = CryptUtil.sha1(StringUtils.trimToNull(url) ?: title)
  d.latLon = source.latLon ?: this.latLng?.toPoint()
  d.html = html
  d.imageUrl = ""
  d.text = StringUtils.trimToEmpty(text)
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
  a.mimeType = type
  a.remoteDataUrl = url
  a.originalUrl = url
  a.size = length
  a.duration = duration
  a.documentId = document.id
  return a
}

fun nextCronDate(cronString: String, from: LocalDateTime): LocalDateTime {
  return CronExpression.parse(cronString).next(from)!!
}
